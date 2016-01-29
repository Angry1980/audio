package angry1980.neo4j.louvain;

import org.neo4j.graphdb.*;
import org.neo4j.tooling.GlobalGraphOperations;

public class DefaultTaskAdapter implements TaskAdapter{

    @Override
    public ResourceIterable<Node> getNodes(GraphDatabaseService g) {
        return GlobalGraphOperations.at(g).getAllNodes();
    }

    @Override
    public Iterable<Relationship> getRelationships(Node node) {
        return node.getRelationships(Direction.BOTH);
    }

    @Override
    public double getInitWeight(Relationship r) {
        return 1.0;
    }

    @Override
    public long getId(Node node) {
        return node.getId();
    }
}
