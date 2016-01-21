package angry1980.audio.dao;

import angry1980.audio.model.Neo4jNodeType;
import angry1980.audio.model.Neo4jRelationType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Neo4jRelation extends Neo4j {

    public Neo4jRelation(GraphDatabaseService graphDB) {
        super(graphDB);
    }

    protected <T> List<T> getConnections(Node node, Neo4jRelationType type, Function<Relationship, T> f) {
        return getConnections(node, type).map(f).collect(Collectors.toList());
    }

    protected <T> Collection<T> getEntities(GraphDatabaseService graphDB, Neo4jNodeType nodeType, long nodeId, Neo4jRelationType type, Function<Relationship, T> f) {
        return getNode(graphDB, nodeType, nodeId)
                .map(node -> getConnections(node, type, f))
                .orElseGet(() -> Collections.emptyList());
    }

}
