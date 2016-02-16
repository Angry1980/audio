package angry1980.audio.neo4j;

import angry1980.audio.model.FingerprintType;
import angry1980.audio.model.ImmutableTrackSimilarity;
import angry1980.audio.model.TrackSimilarity;
import angry1980.neo4j.Query;
import com.google.common.collect.ImmutableMap;
import org.neo4j.graphdb.Result;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FingerprintTypeSimilaritiesQuery implements Query<FingerprintTypeSimilaritiesQuery> {

    private static final String QUERY = "match (cluster1)<-[:IS]-(track1)-[similar:SIMILAR]->(track2)-[:IS]->(cluster2)"
            + " where similar.type={fingerprintType} and similar.weight > {minWeight}"
            + " return track1.id as t1, track2.id as t2, similar.weight as value, cluster1=cluster2 as tp"
            ;

    private final FingerprintType type;
    private final boolean truthPositive;
    private List<TrackSimilarity> result = Collections.emptyList();

    public FingerprintTypeSimilaritiesQuery(FingerprintType type, boolean truthPositive) {
        this.type = type;
        this.truthPositive = truthPositive;
    }

    @Override
    public String getQuery() {
        return QUERY;
    }

    @Override
    public Map<String, Object> getParams() {
        return ImmutableMap.of("fingerprintType", type.name(), "minWeight", 1);
    }

    public List<TrackSimilarity> getResult() {
        return result;
    }

    @Override
    public FingerprintTypeSimilaritiesQuery handle(Result result) {
        this.result = Query.asStream(result)
                .filter(data -> truthPositive ^ Query.getBooleanValue(data, "tp").orElse(false))
                .map(data -> ImmutableTrackSimilarity.builder()
                                .track1(Query.getLongValue(data, "t1").orElse(0L))
                                .track2(Query.getLongValue(data, "t2").orElse(0L))
                                .value(Query.getIntValue(data, "value").orElse(0))
                                .fingerprintType(type)
                                .build()
                ).collect(Collectors.toList());
        return this;
    }
}
