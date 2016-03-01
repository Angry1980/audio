package angry1980.audio.neo4j;

import angry1980.audio.model.ComparingType;
import angry1980.neo4j.Query;
import com.google.common.collect.ImmutableMap;
import org.neo4j.graphdb.Result;

import java.util.Map;
import java.util.Objects;

public class UniqueCountQuery implements Query<UniqueCountQuery> {

    private static final String QUERY = "match (cluster1)<-[:IS]-(track1)-[similar:SIMILAR]->(:TRACK)-[:IS]->(cluster1)"
        + " where similar.type <> {type} or similar.weight > {minWeight}"
        + " with track1.id as t1, collect(distinct(similar.type)) as types"
        + " where length(types) = 1 and {type} in types"
        + " return count(distinct(t1)) as result"
        ;

    private final int minWeight;
    private final ComparingType type;
    private int result;

    public UniqueCountQuery(ComparingType type) {
        this(type, 0);
    }

    public UniqueCountQuery(ComparingType type, int minWeight) {
        this.minWeight = minWeight;
        this.type = Objects.requireNonNull(type);
    }

    public int getResult() {
        return result;
    }

    @Override
    public String getQuery() {
        return QUERY;
    }

    @Override
    public Map<String, Object> getParams() {
        return ImmutableMap.of("type", type.name(), "minWeight", minWeight);
    }

    @Override
    public UniqueCountQuery handle(Result result) {
        this.result = Query.getIntResult(result, "result");
        return this;
    }
}
