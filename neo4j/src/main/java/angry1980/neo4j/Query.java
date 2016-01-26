package angry1980.neo4j;

import org.neo4j.graphdb.Result;

import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface Query<T> {

    static Stream<Map<String, Object>> asStream(Result result){
        Iterable<Map<String, Object>> iterable = () -> result;
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    String getQuery();

    Map<String, Object> getParams();

    T handle(Result result);
}
