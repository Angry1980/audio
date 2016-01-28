package angry1980.audio.neo4j;

import angry1980.audio.model.Neo4jNodeType;
import angry1980.audio.model.Neo4jRelationType;
import angry1980.neo4j.louvain.TaskAdapter;
import org.neo4j.graphdb.*;
import org.neo4j.tooling.GlobalGraphOperations;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class LouvainTaskAdapter implements TaskAdapter{

    @Override
    public ResourceIterable<Node> getNodes(GraphDatabaseService g) {
        return () -> g.findNodes(Neo4jNodeType.TRACK);
    }

    @Override
    public Iterable<Relationship> getRelationships(GraphDatabaseService g) {
        return StreamSupport.stream(GlobalGraphOperations.at(g).getAllRelationships().spliterator(), false)
                    .filter(r -> r.isType(Neo4jRelationType.SIMILAR))
                    .collect(Collectors.toList());
    }

    @Override
    public Iterable<Relationship> getRelationships(Node node) {
        return node.getRelationships(Neo4jRelationType.SIMILAR, Direction.OUTGOING);
    }

    @Override
    public double getInitWeight(Relationship r) {
        return 1.0;
    }
}
