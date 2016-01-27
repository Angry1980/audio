package angry1980.neo4j;

import org.neo4j.graphdb.Result;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface Query<K extends Query<K>> {

    static Stream<Map<String, Object>> asStream(Result result){
        Iterable<Map<String, Object>> iterable = () -> result;
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    static Optional<Integer> getIntValue(Map<String, Object> data, String name){
        return Optional.ofNullable(data.get(name))
                .map(Object::toString)
                .map(Integer::decode);
    }

    static Optional<Boolean> getBooleanValue(Map<String, Object> data, String name){
        return Optional.ofNullable(data.get(name))
                .map(Object::toString)
                .map(Boolean::parseBoolean);
    }

    String getQuery();

    Map<String, Object> getParams();

    K handle(Result result);
}
