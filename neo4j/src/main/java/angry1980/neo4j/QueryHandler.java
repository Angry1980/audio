package angry1980.neo4j;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;

import java.util.Objects;

public class QueryHandler {

    private GraphDatabaseService graphDB;

    public QueryHandler(GraphDatabaseService graphDB) {
        this.graphDB = Objects.requireNonNull(graphDB);
    }

    public <T> T execute(Query<T> query) {
        try (Result result = graphDB.execute(query.getQuery(), query.getParams())) {
            return query.handle(result);
        }
    }
}
