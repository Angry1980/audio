package angry1980.audio.neo4j;

import angry1980.audio.model.FingerprintType;
import angry1980.audio.stats.ImmutableFingerprintTypeData;
import angry1980.audio.stats.ImmutableStats;
import angry1980.audio.stats.Stats;
import angry1980.neo4j.Query;
import org.neo4j.graphdb.Result;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class FingerprintTypeComparingAllQuery implements Query<FingerprintTypeComparingAllQuery>{

    private static final String QUERY = "match (cluster1)<-[:IS]-(track1:TRACK)-[similar1:SIMILAR{type:{type1}}]->(track2:TRACK)-[:IS]->(cluster2)"
            + " where similar1.weight > {minWeight1}"
            + " optional match (track1)-[similar2:SIMILAR{type:{type2}}]->(track2) where similar2.weight > {minWeight2}"
            + " optional match (track1)-[similar3:SIMILAR{type:{type3}}]->(track2) where similar3.weight > {minWeight3}"
            + " with track1 as track, ((similar2 is null) or (similar3 is null)) as empty, cluster1=cluster2 as tp"
            + " return count(distinct(track)) as result, not(empty) as common, tp "
            ;

    private Map<FingerprintType, Integer> minWeights;
    private Stats result;

    public FingerprintTypeComparingAllQuery(Map<FingerprintType, Integer> minWeights) {
        this.minWeights = Objects.requireNonNull(minWeights);
    }

    @Override
    public String getQuery() {
        return QUERY;
    }

    public Stats getResult() {
        return result;
    }

    @Override
    public Map<String, Object> getParams() {
        Map<String, Object> result = new HashMap<>();
        int i = 0;
        for(FingerprintType type : FingerprintType.values()){
            i++;
            result.put("type" + i, type.name());
            result.put("minWeight" + i, minWeights.get(type));
        }
        return result;

    }

    @Override
    public FingerprintTypeComparingAllQuery handle(Result result) {
        Map<Boolean, Integer> map = Query.asStream(result)
                .filter(data -> Query.getBooleanValue(data, "common").orElse(true))
                .collect(
                        Collectors.toMap(
                                data -> Query.getBooleanValue(data, "tp").orElse(true),
                                data -> Query.getIntValue(data, "result").orElse(0)
                        )
                );
        this.result = ImmutableStats.builder()
                .addTypes(ImmutableFingerprintTypeData.builder().type(FingerprintType.CHROMAPRINT).weight(minWeights.get(FingerprintType.CHROMAPRINT)).build())
                .addTypes(ImmutableFingerprintTypeData.builder().type(FingerprintType.LASTFM).weight(minWeights.get(FingerprintType.LASTFM)).build())
                .addTypes(ImmutableFingerprintTypeData.builder().type(FingerprintType.PEAKS).weight(minWeights.get(FingerprintType.PEAKS)).build())
                .falsePositive(map.getOrDefault(false, 0))
                .truePositive(map.getOrDefault(true, 0))
                .build();
        return this;
    }

}
