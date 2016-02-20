package angry1980.audio.neo4j;

import angry1980.audio.model.FingerprintType;
import angry1980.audio.stats.ImmutableFingerprintTypeData;
import angry1980.audio.stats.ImmutableStats;
import angry1980.audio.stats.Stats;
import angry1980.neo4j.Query;
import com.google.common.collect.ImmutableMap;
import org.neo4j.graphdb.Result;

import java.util.*;
import java.util.stream.Collectors;

public class FingerprintTypeComparingQuery implements Query<FingerprintTypeComparingQuery>{

    private static final String QUERY = "match (cluster1)<-[:IS]-(track1:TRACK)-[similar1:SIMILAR{type:{type1}}]->(track2:TRACK)-[:IS]->(cluster2)"
        + " where similar1.weight > {minWeight1}"
        + " optional match (track1)-[similar2:SIMILAR{type:{type2}}]->(track2)"
        + " where similar2.weight > {minWeight2}"
        + " with (similar2 is null) as empty, track1 as track, cluster1=cluster2 as tp"
        + " return count(distinct(track)) as result, not(empty) as common, tp "
        ;

    private final FingerprintType type1;
    private final FingerprintType type2;
    private Map<FingerprintType, Integer> minWeights;
    private Stats result;

    public FingerprintTypeComparingQuery(Map<FingerprintType, Integer> minWeights, FingerprintType type1, FingerprintType type2) {
        this.minWeights = Objects.requireNonNull(minWeights);
        this.type1 = Objects.requireNonNull(type1);
        this.type2 = Objects.requireNonNull(type2);
    }

    @Override
    public String getQuery() {
        return QUERY;
    }

    @Override
    public Map<String, Object> getParams() {
        return ImmutableMap.of("type1", type1.name(), "type2", type2.name(),
                        "minWeight1", minWeights.get(type1), "minWeight2", minWeights.get(type2));
    }

    @Override
    public FingerprintTypeComparingQuery handle(Result result) {
        Map<Boolean, Integer> map = Query.asStream(result)
                .filter(data -> Query.getBooleanValue(data, "common").orElse(true))
                .collect(
                        Collectors.toMap(
                                data -> Query.getBooleanValue(data, "tp").orElse(true),
                                data -> Query.getIntValue(data, "result").orElse(0)
                        )
                );
        this.result = ImmutableStats.builder()
                    .addTypes(ImmutableFingerprintTypeData.builder().type(type1).weight(minWeights.get(type1)).build())
                    .addTypes(ImmutableFingerprintTypeData.builder().type(type2).weight(minWeights.get(type2)).build())
                    .falsePositive(map.getOrDefault(false, 0))
                    .truePositive(map.getOrDefault(true, 0))
                        .build();
        return this;
    }

    public Stats getResult() {
        return result;
    }

}
