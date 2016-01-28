package angry1980.neo4j.louvain;

import it.unimi.dsi.fastutil.longs.*;
import org.neo4j.graphdb.*;
import org.neo4j.tooling.GlobalGraphOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * was ported from here https://github.com/besil/Neo4jSNA
 */
public class Louvain {

    private final Logger LOG = LoggerFactory.getLogger(Louvain.class);

    private final String layerProperty = "layer", weightProperty = "clusterWeight", idProperty= "id";
    private final GraphDatabaseService g;
    private final double totalEdgeWeight;
    private final LouvainResult louvainResult;
    private final int batchSize = 100_000;
    private Label layerLabel, newLayerLabel;
    private int layerCount = 0;
    private int macroNodeCount = 0;
    private TaskAdapter adapter;
    private Long2ObjectMap<Node> nodes = new Long2ObjectArrayMap<>();
    private Long2ObjectMap<Node> macros = new Long2ObjectArrayMap<>();
    private Long2LongMap communities = new Long2LongArrayMap();

    public Louvain(GraphDatabaseService g){
        this(g, new DefaultTaskAdapter());
    }

    public Louvain(GraphDatabaseService g, TaskAdapter adapter) {
        this.louvainResult = new LouvainResult();
        this.g = g;
        this.adapter = adapter;
        this.layerLabel = DynamicLabel.label("layerLabel");
        this.newLayerLabel = DynamicLabel.label("newLayerLabel");
        try (Transaction tx = g.beginTx()) {
            for (Node n : adapter.getNodes(g)) {
                n.addLabel(this.layerLabel);
                n.setProperty(layerProperty, layerCount);
                //n.addLabel(this.communityLabel);
                //n.setProperty(communityProperty, n.getId());
                nodes.put((long)n.getProperty(idProperty), n);
                communities.put((long)n.getProperty(idProperty), (long)n.getProperty(idProperty));
            }
            tx.success();
        }

        double edgeWeight = 0.0;
        try (Transaction tx = g.beginTx()) {
            for (Relationship r : adapter.getRelationships(g)) {
                edgeWeight += weight(r);
            }
            tx.success();
        }
        totalEdgeWeight = edgeWeight;
    }

    public void execute() {
        do {
            LOG.info("Layer count: " + layerCount);
            macroNodeCount = this.pass();
        } while (macroNodeCount != 0);
    }

    public int pass() {
        this.firstPhase();
        LOG.info("Starting modularity...");
        int totMacroNodes = this.secondPhase();
        LOG.info("Created " + totMacroNodes);

        layerCount++;
        return totMacroNodes;
    }

    public void firstPhase() {
        int movements;
        int counterOps = 0;

        Transaction tx = g.beginTx();

        do {
            movements = 0;
            // itera solo per i nodi del livello corrente
            ResourceIterator<Node> nodes = g.findNodes(layerLabel, layerProperty, layerCount);
            while (nodes.hasNext()) {
                Node src = nodes.next();
                long srcCommunity = communities.get((long)src.getProperty(idProperty));
                long bestCommunity = srcCommunity;
                double bestDelta = 0.0;

                Iterable<Relationship> rels = layerCount == 0 ? adapter.getRelationships(src) : src.getRelationships(Direction.BOTH, LouvainRels.NewEdges);
                for (Relationship r : rels) {
                    Node neigh = r.getOtherNode(src);
                    if (src.equals(neigh)) {
                        continue;
                    }
                    long neighCommunity = communities.get((long)neigh.getProperty(idProperty));

                    double delta = this.calculateDelta(src, srcCommunity, neighCommunity);
                    if (delta > bestDelta) {
                        bestDelta = delta;
                        bestCommunity = neighCommunity;
                    }
                }

                if (srcCommunity != bestCommunity) {
                    communities.put((long)src.getProperty(idProperty), bestCommunity);
                    tx = this.batchCommit(++counterOps, tx, g);
                    movements++;
                }
            }
            LOG.info("Movements so far: " + movements);
        } while (movements != 0);

        tx.success();
        tx.close();
    }

    private double calculateDelta(Node n, long srcCommunity, long dstCommunity) {
        double first, second;

        first = this.communityWeightWithout(n, dstCommunity) - this.communityWeightWithout(n, srcCommunity);
        first = first / totalEdgeWeight;

        second = (this.communityVolumeWithout(n, srcCommunity) - this.communityVolumeWithout(n, dstCommunity)) * nodeVolume(n);
        second = second / (2 * Math.pow(totalEdgeWeight, 2));

        return first + second;
    }

    private double weight(Relationship r) {
        return r.hasProperty(weightProperty) ? (double) r.getProperty(weightProperty) : adapter.getInitWeight(r);
    }

    private double communityWeightWithout(Node n, long cId) {
        double weight = 0.0;
        for (Relationship r : adapter.getRelationships(n)) {
            Node other = r.getOtherNode(n);
            if (other.equals(n)){
                continue;
            }
            if (communities.get((long)other.getProperty(idProperty)) == cId){
                weight += this.weight(r);
            }
        }
        return weight;
    }

    private double nodeVolume(Node n) {
        double vol = 0;
        for (Relationship r : adapter.getRelationships(n)) {
            vol += this.weight(r);   // put here the edge weight

            if (r.getOtherNode(n).equals(n))
                vol += this.weight(r);
        }
        return vol;
    }

    private double communityVolumeWithout(Node n, long cId) {
        return communities.entrySet().stream()
                .filter(entry -> entry.getValue() == cId)
                .map(entry -> nodes.get(entry.getKey()))
                .filter(member -> !member.equals(n))
                .reduce(0.0, (vol, member) -> vol + nodeVolume(member) , (v1, v2) -> v1 + v2);
    }

    public int secondPhase() {
        int totMacroNodes = 0;
        long counterOps = 0;

        Transaction tx = g.beginTx();

        // Check if a new layer must be created
        LongSet macroNodesCommunities = new LongOpenHashSet();
        ResourceIterator<Node> checkNodes = g.findNodes(layerLabel, layerProperty, layerCount);
        while (checkNodes.hasNext()) {
            Node n = checkNodes.next();
            macroNodesCommunities.add(communities.get((long)n.getProperty(idProperty)));
        }

        if (macroNodesCommunities.size() == macroNodeCount) {
            // Nothing to move: save to layer object and exit
            LouvainLayer louvainLayer = louvainResult.layer(layerCount);
            ResourceIterator<Node> activeNodes = g.findNodes(layerLabel, layerProperty, layerCount);
            while (activeNodes.hasNext()) {
                Node activeNode = activeNodes.next();
                long activeNodeId = activeNode.hasProperty(idProperty) ? (long) activeNode.getProperty(idProperty) : activeNode.getId();
                long cId = communities.get((long)activeNode.getProperty(idProperty));

                louvainLayer.add(activeNodeId, cId);
            }

            return totMacroNodes;
        }

        int count = 0;
        LouvainLayer louvainLayer = louvainResult.layer(layerCount);
        // Get all nodes of current layer
        ResourceIterator<Node> activeNodes = g.findNodes(layerLabel, layerProperty, layerCount);
        while (activeNodes.hasNext()) {
            if (++count % 1000 == 0)
                LOG.info("Computed " + count + " nodes");
            Node activeNode = activeNodes.next();
            long activeNodeId = activeNode.hasProperty(idProperty) ? (long)activeNode.getProperty(idProperty) : activeNode.getId();
            long cId = communities.get((long)activeNode.getProperty(idProperty));

            louvainLayer.add(activeNodeId, cId);

            // Prendi il macronode associato a questa community
            Node macroNode = macros.get(cId);
            if (macroNode == null) {    // Se non esiste, crealo
                totMacroNodes++;
                macroNode = g.createNode(newLayerLabel);
                macroNode.setProperty(idProperty, cId);
                macros.put(cId, macroNode);
                macroNode.setProperty(layerProperty, layerCount + 1); // e' il nuovo layer

            }

            // Create a relationship to the original node
            activeNode.createRelationshipTo(macroNode, LouvainRels.Layer);
            activeNode.removeLabel(layerLabel);
        }

        tx.success();
        tx.close();
        tx = g.beginTx();


        ResourceIterator<Node> macroNodes = g.findNodes(newLayerLabel);
        while (macroNodes.hasNext()) {
            Node macroNode = macroNodes.next();

            for (Relationship layer : macroNode.getRelationships(Direction.INCOMING, LouvainRels.Layer)) {
                Node originalNode = layer.getOtherNode(macroNode);

                for (Relationship r : adapter.getRelationships(originalNode)) {
                    if (!r.isType(LouvainRels.Layer)) {
                        Node neigh = r.getOtherNode(originalNode);
                        Node otherMacroNode = neigh.getSingleRelationship(LouvainRels.Layer, Direction.OUTGOING).getOtherNode(neigh);

                        Relationship macroRel = getRelationshipBetween(macroNode, otherMacroNode, Direction.BOTH, LouvainRels.NewEdges);
                        if (macroRel == null) {
                            macroRel = macroNode.createRelationshipTo(otherMacroNode, LouvainRels.NewEdges);
                            tx = this.batchCommit(++counterOps, tx, g);
                            macroRel.setProperty(weightProperty, 0.0);
                            tx = this.batchCommit(++counterOps, tx, g);
                        }
                        double w = (double) macroRel.getProperty(weightProperty);
                        macroRel.setProperty(weightProperty, w + 1.0);
                        tx = this.batchCommit(++counterOps, tx, g);
                    }
                }
            }
        }

        ResourceIterator<Node> macros = g.findNodes(newLayerLabel);
        while (macros.hasNext()) {
            Node next = macros.next();
            next.removeLabel(newLayerLabel);
            tx = this.batchCommit(++counterOps, tx, g);
            next.addLabel(layerLabel);
            tx = this.batchCommit(++counterOps, tx, g);
        }

        tx.success();
        tx.close();

        return totMacroNodes;
    }

    private Transaction batchCommit(long counterOps, Transaction tx, GraphDatabaseService g) {
        if (++counterOps % batchSize == 0) {
            LOG.info("Committing...");
            tx.success();
            tx.close();
            tx = g.beginTx();
        }
        return tx;
    }

    private Relationship getRelationshipBetween(Node n1, Node n2, Direction dir, RelationshipType... relTypes) {
        for (Relationship rel : n1.getRelationships(dir, relTypes)) {
            if (rel.getOtherNode(n1).equals(n2)) return rel;
        }
        return null;
    }

    public LouvainResult getResult() {
        return this.louvainResult;
    }

    enum LouvainRels implements RelationshipType {
        Layer, NewEdges
    }
}
