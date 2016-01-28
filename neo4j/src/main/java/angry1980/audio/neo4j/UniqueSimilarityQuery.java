package angry1980.audio.neo4j;

import angry1980.audio.model.FingerprintType;
import angry1980.neo4j.Query;
import com.google.common.collect.ImmutableMap;
import org.neo4j.graphdb.Result;

import java.util.Map;
import java.util.Objects;

public class UniqueSimilarityQuery implements Query<UniqueSimilarityQuery> {

    private static final String QUERY = "match (cluster1)<-[:IS]-(track1:TRACK)-[similar:SIMILAR]->(track2:TRACK)-[:IS]->(cluster1)"
            + " with track1.id as t1, track2.id as t2, count(similar) as sc, collect(similar.type) as types"
            + " where sc = 1 and {type} in types"
            + " return count(sc) as result"
            ;

    private final FingerprintType type;
    private int result;

    public UniqueSimilarityQuery(FingerprintType type) {
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
        return ImmutableMap.of("type", type.name());
    }

    @Override
    public UniqueSimilarityQuery handle(Result result) {
        this.result = Query.getIntResult(result, "result");
        return this;
    }
}
