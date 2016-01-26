package angry1980.audio.dao;

import angry1980.audio.model.Neo4jNodeType;
import angry1980.audio.model.Neo4jRelationType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Neo4jNode extends Neo4j {

    public Neo4jNode(GraphDatabaseService graphDB) {
        super(graphDB);
    }

    protected Optional<Node> getNode(GraphDatabaseService graphDB, long id){
        return getNode(graphDB, getType(), id);
    }

    protected <T> T getEntity(GraphDatabaseService graphDB, long id, Function<Node, T> f){
        return getNode(graphDB, id).map(f).orElse(null);
    }

    protected Node getOrCreateNode(GraphDatabaseService graphDB, long id){
        return getOrCreateNode(graphDB, id, Function.identity());
    }

    protected Node getOrCreateNode(GraphDatabaseService graphDB, long id, Function<Node, Node> create){
        return getOrCreateNode(graphDB, getType(), id, create);
    }

    protected Node getOrCreateNode(GraphDatabaseService graphDB, Neo4jNodeType type, long id){
        return getOrCreateNode(graphDB, type, id, Function.identity());
    }

    protected Stream<Node> getNodesAsStream(GraphDatabaseService graphDB) {
        return getNodesAsStream(graphDB, getType());
    }

    protected <T> Collection<T> getAllEntities(GraphDatabaseService graphDB, Function<Node, T> f) {
        return getNodesAsStream(graphDB).map(f).collect(Collectors.toList());
    }

    protected Stream<Node> getConnectedNodes(Node node, Neo4jRelationType type){
        return getNodeConnectionsAsStream(node, type).map(r -> r.getEndNode());
    }

    protected <T> Collection<T> getConnectedEntities(Node node, Neo4jRelationType type, Function<Node, T> f) {
        return getConnectedNodes(node, type).map(f).collect(Collectors.toList());
    }

    protected <T> Collection<T> getConnectedEntities(GraphDatabaseService graphDB, Neo4jNodeType nodeType, long nodeId, Neo4jRelationType type, Function<Node, T> f) {
        return getNode(graphDB, nodeType, nodeId)
                .map(node -> getConnectedEntities(node, type, f))
                .orElseGet(() -> Collections.emptyList());
    }

    protected abstract Neo4jNodeType getType();
}
