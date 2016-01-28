package angry1980.neo4j.louvain;

import it.unimi.dsi.fastutil.longs.*;
import org.neo4j.graphdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Louvain {

    private final Logger LOG = LoggerFactory.getLogger(Louvain.class);

    private final String weightProperty = "clusterWeight";
    private final GraphDatabaseService g;
    private final double totalEdgeWeight;
    private final LouvainResult louvainResult;
    private int layerCount = 0;
    private int macroNodeCount = 0;
    private TaskAdapter adapter;
    private Long2ObjectMap<Node> nodes = new Long2ObjectArrayMap<>();

    private Long2LongMap communities = new Long2LongArrayMap();

    public Louvain(GraphDatabaseService g){
        this(g, new DefaultTaskAdapter());
    }

    public Louvain(GraphDatabaseService g, TaskAdapter adapter) {
        this.louvainResult = new LouvainResult();
        this.g = g;
        this.adapter = adapter;
        try (Transaction tx = g.beginTx()) {
            for (Node n : adapter.getNodes(g)) {
                nodes.put(adapter.getId(n), n);
                communities.put(adapter.getId(n), adapter.getId(n));
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

    private long getCommunity(Node node){
        return communities.get(adapter.getId(node));
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
            for(Node src : nodes.values()){
                long srcCommunity = getCommunity(src);
                long bestCommunity = srcCommunity;
                double bestDelta = 0.0;

                Iterable<Relationship> rels = layerCount == 0 ? adapter.getRelationships(src) : src.getRelationships(Direction.BOTH, LouvainRels.NewEdges);
                for (Relationship r : rels) {
                    Node neigh = r.getOtherNode(src);
                    if (src.equals(neigh)) {
                        continue;
                    }
                    long neighCommunity = getCommunity(neigh);

                    double delta = this.calculateDelta(src, srcCommunity, neighCommunity);
                    if (delta > bestDelta) {
                        bestDelta = delta;
                        bestCommunity = neighCommunity;
                    }
                }

                if (srcCommunity != bestCommunity) {
                    communities.put(adapter.getId(src), bestCommunity);
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
            if (getCommunity(other) == cId){
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
        Long2ObjectMap<Node> macros = new Long2ObjectArrayMap<>();
        Transaction tx = g.beginTx();

        // Check if a new layer must be created
        LongSet macroNodesCommunities = new LongOpenHashSet();
        for(Node n : nodes.values()){
            macroNodesCommunities.add(getCommunity(n));
        }

        if (macroNodesCommunities.size() == macroNodeCount) {
            // Nothing to move: save to layer object and exit
            LouvainLayer louvainLayer = louvainResult.layer(layerCount);
            for(Node activeNode : nodes.values()){
                louvainLayer.add(adapter.getId(activeNode), getCommunity(activeNode));
            }

            return totMacroNodes;
        }

        LouvainLayer louvainLayer = louvainResult.layer(layerCount);
        // Get all nodes of current layer
        for(Node activeNode : nodes.values()){
            long activeNodeId = adapter.getId(activeNode);
            long cId = getCommunity(activeNode);

            louvainLayer.add(activeNodeId, cId);

            // Prendi il macronode associato a questa community
            Node macroNode = macros.get(cId);
            if (macroNode == null) {    // Se non esiste, crealo
                totMacroNodes++;
                macroNode = g.createNode();
                adapter.setId(macroNode, cId);
                macros.put(cId, macroNode);
            }

            // Create a relationship to the original node
            activeNode.createRelationshipTo(macroNode, LouvainRels.Layer);
        }
        nodes.clear();
        tx.success();
        tx.close();
        tx = g.beginTx();

        for(Node macroNode : macros.values()){
            for (Relationship layer : macroNode.getRelationships(Direction.INCOMING, LouvainRels.Layer)) {
                Node originalNode = layer.getOtherNode(macroNode);

                for (Relationship r : adapter.getRelationships(originalNode)) {
                    if (!r.isType(LouvainRels.Layer)) {
                        Node neigh = r.getOtherNode(originalNode);
                        Node otherMacroNode = neigh.getSingleRelationship(LouvainRels.Layer, Direction.OUTGOING).getOtherNode(neigh);

                        Relationship macroRel = getRelationshipBetween(macroNode, otherMacroNode, Direction.BOTH, LouvainRels.NewEdges);
                        if (macroRel == null) {
                            macroRel = macroNode.createRelationshipTo(otherMacroNode, LouvainRels.NewEdges);
                            macroRel.setProperty(weightProperty, 0.0);
                        }
                        double w = (double) macroRel.getProperty(weightProperty);
                        macroRel.setProperty(weightProperty, w + 1.0);
                    }
                }
            }

        }
        nodes = macros;
        tx.success();
        tx.close();

        return totMacroNodes;
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
