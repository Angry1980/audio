package angry1980.audio.neo4j;

import angry1980.audio.model.ComparingType;
import angry1980.audio.model.Neo4jNodeType;
import angry1980.audio.model.Neo4jRelationType;
import angry1980.neo4j.louvain.TaskAdapter;
import org.neo4j.graphdb.*;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class LouvainTaskAdapter implements TaskAdapter{

    private Map<ComparingType, Integer> minWeights;

    public LouvainTaskAdapter(Map<ComparingType, Integer> minWeights) {
        this.minWeights = Objects.requireNonNull(minWeights);
    }

    @Override
    public ResourceIterable<Node> getNodes(GraphDatabaseService g) {
        return () -> g.findNodes(Neo4jNodeType.TRACK);
    }

    @Override
    public Iterable<Relationship> getRelationships(Node node) {
        return StreamSupport.stream(node.getRelationships(Neo4jRelationType.SIMILAR, Direction.OUTGOING).spliterator(), false)
                .filter(r -> {
                    int weight = minWeights.getOrDefault(ComparingType.valueOf((String) r.getProperty("type")), 0);
                    return Integer.compare((Integer)r.getProperty("weight"), weight) > 0;
                })
                .collect(Collectors.toList());
    }

    @Override
    public double getInitWeight(Relationship r) {
        return 1.0;
    }

    @Override
    public long getId(Node node) {
        return node.hasProperty("id")? (long)node.getProperty("id") : node.getId();
    }

}
