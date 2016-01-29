package angry1980.audio.neo4j;

import angry1980.audio.model.Neo4jNodeType;
import angry1980.audio.model.Neo4jRelationType;
import angry1980.neo4j.louvain.TaskAdapter;
import org.neo4j.graphdb.*;

public class LouvainTaskAdapter implements TaskAdapter{

    @Override
    public ResourceIterable<Node> getNodes(GraphDatabaseService g) {
        return () -> g.findNodes(Neo4jNodeType.TRACK);
    }

    @Override
    public Iterable<Relationship> getRelationships(Node node) {
        return node.getRelationships(Neo4jRelationType.SIMILAR, Direction.OUTGOING);
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
