package angry1980.neo4j.louvain;

import it.unimi.dsi.fastutil.longs.*;
import org.neo4j.graphdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Louvain {

    private final Logger LOG = LoggerFactory.getLogger(Louvain.class);

    private final double totalEdgeWeight;
    private final LouvainResult louvainResult;
    private int layerCount = 0;
    private Long2ObjectMap<LNode> nodes = new Long2ObjectArrayMap<>();

    //todo: use LNode list as constructor argument
    public Louvain(GraphDatabaseService g){
        this(g, new DefaultTaskAdapter());
    }

    public Louvain(GraphDatabaseService g, TaskAdapter adapter) {
        this.louvainResult = new LouvainResult();
        try (Transaction tx = g.beginTx()) {
            for (Node n : adapter.getNodes(g)) {
                List<LRel> rels = new ArrayList<>();
                for(Relationship r : adapter.getRelationships(n)){
                    rels.add(new LRel(adapter.getInitWeight(r), adapter.getId(r.getOtherNode(n))));
                }
                LNode ln = new LNode(adapter.getId(n), adapter.getId(n), rels);
                nodes.put(ln.id, ln);
            }
            tx.success();
        }
        totalEdgeWeight = nodes.values().stream().mapToDouble(LNode::getWeight).sum();
    }

    public void execute() {
        int macroNodeCount = 0;
        do {
            LOG.info("Layer count: " + layerCount);
            macroNodeCount = this.pass(macroNodeCount);
        } while (macroNodeCount != 0);
    }

    public int pass(int macroNodeCount) {
        this.firstPhase();
        LOG.info("Starting modularity...");
        int totMacroNodes = this.secondPhase(macroNodeCount);
        LOG.info("Created " + totMacroNodes);

        layerCount++;
        return totMacroNodes;
    }

    public void firstPhase() {
        int movements;

        do {
            movements = 0;
            for(LNode src : nodes.values()){
                long bestCommunity = src.community;
                double bestDelta = 0.0;
                for (LRel r : src.rels) {
                    long neighCommunity = nodes.get(r.otherNode).community;

                    double delta = this.calculateDelta(src, src.community, neighCommunity);
                    if (delta > bestDelta) {
                        bestDelta = delta;
                        bestCommunity = neighCommunity;
                    }
                }

                if (src.community != bestCommunity) {
                    src.community = bestCommunity;
                    movements++;
                }
            }
            LOG.info("Movements so far: " + movements);
        } while (movements != 0);
    }

    private double calculateDelta(LNode n, long srcCommunity, long dstCommunity) {
        double first, second;

        first = n.communityWeightWithout(dstCommunity) - n.communityWeightWithout(srcCommunity);
        first = first / totalEdgeWeight;

        second = (n.communityVolumeWithout(srcCommunity) - n.communityVolumeWithout(dstCommunity)) * n.getWeight();
        second = second / (2 * Math.pow(totalEdgeWeight, 2));

        return first + second;
    }

    public int secondPhase(int macroNodeCount) {
        int totMacroNodes = 0;
        Long2ObjectMap<LNode> macros = new Long2ObjectArrayMap<>();
        Map<LNode, LNode> originalToMacro = new HashMap<>();
        LongSet macroNodesCommunities = new LongOpenHashSet();
        LouvainLayer louvainLayer = louvainResult.layer(layerCount);
        for(LNode n : nodes.values()){
            macroNodesCommunities.add(n.community);
            louvainLayer.add(n.id, n.community);
        }

        // Check if a new layer must be created
        if (macroNodesCommunities.size() == macroNodeCount) {
            // Nothing to move: save to layer object and exit
            return totMacroNodes;
        }

        // Get all nodes of current layer
        for(LNode activeNode : nodes.values()){

            // Prendi il macronode associato a questa community
            LNode macroNode = macros.get(activeNode.community);
            if (macroNode == null) {    // Se non esiste, crealo
                totMacroNodes++;
                macroNode = new LNode(activeNode.community, activeNode.community, new ArrayList<>());
                macros.put(macroNode.id, macroNode);
            }

            // Create a relationship to the original node
            originalToMacro.put(activeNode, macroNode);
        }

        for(Map.Entry<LNode, LNode> entry : originalToMacro.entrySet()){
            LNode macroNode = entry.getValue();
            for (LRel r : entry.getKey().rels) {
                LRel macroRel = macroNode.tryToFindRel(r.otherNode)
                        .orElseGet(() -> {
                            LRel mr = new LRel(0.0, originalToMacro.get(nodes.get(r.otherNode)).id);
                            macroNode.rels.add(mr);
                            return mr;
                        });
                macroRel.weight = macroRel.weight + 1.0;
            }
        }
        nodes = macros;
        return totMacroNodes;
    }

    public LouvainResult getResult() {
        return this.louvainResult;
    }

    class LNode{
        long id;
        List<LRel> rels;
        double weight;
        long community;

        public LNode(long id, long community, List<LRel> rels) {
            this.id = id;
            this.rels = rels;
            this.community = community;
        }

        public long getCommunity() {
            return community;
        }

        public double getWeight(){
            if(weight == 0){
                weight = rels.stream().mapToDouble(LRel::getWeight).sum();
            }
            return weight;
        }

        public Optional<LRel> tryToFindRel(long other){
            return rels.stream()
                    .filter(r -> r.otherNode == other)
                    .findAny();
        }

        public double communityWeightWithout(long cId) {
            return rels.stream()
                    .filter(r -> nodes.get(r.otherNode).community == cId)
                    .mapToDouble(LRel::getWeight)
                    .sum();
        }

        public double communityVolumeWithout(long cId) {
            return nodes.values().stream()
                    .filter(n -> n.community == cId)
                    .filter(n -> !n.equals(this))
                    .mapToDouble(LNode::getWeight)
                    .sum();
        }

    }

    class LRel{
        double weight;
        long otherNode;

        public LRel(double weight, long otherNode) {
            this.weight = weight;
            this.otherNode = otherNode;
        }

        public double getWeight() {
            return weight;
        }
    }
}
