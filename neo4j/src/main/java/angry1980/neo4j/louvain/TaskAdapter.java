package angry1980.neo4j.louvain;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterable;

public interface TaskAdapter {

    ResourceIterable<Node> getNodes(GraphDatabaseService g);
    Iterable<Relationship> getRelationships(Node node);
    double getInitWeight(Relationship r);
    long getId(Node node);
}
