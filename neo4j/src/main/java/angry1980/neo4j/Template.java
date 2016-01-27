package angry1980.neo4j;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class Template {

    private GraphDatabaseService graphDB;

    public Template(GraphDatabaseService graphDB) {
        this.graphDB = Objects.requireNonNull(graphDB);
    }

    public <T> T execute(Function<GraphDatabaseService, T> f){
        try(Transaction tx = graphDB.beginTx()){
            T result = f.apply(graphDB);
            tx.success();
            return result;
        }
    }

    public void execute(Consumer<GraphDatabaseService> c){
        try(Transaction tx = graphDB.beginTx()){
            c.accept(graphDB);
            tx.success();
        }
    }

    public <K extends Query<K>> K executeQuery(K query){
        return execute(graphDB -> {
            return this.handle(graphDB, query);
        });
    }

    public <K extends Query<K>> K handle(K query) {
        return handle(graphDB, query);
    }

    private <K extends Query<K>> K handle(GraphDatabaseService graphDB, K query){
        try (Result result = graphDB.execute(query.getQuery(), query.getParams())) {
            return query.handle(result);
        }
    }
}
