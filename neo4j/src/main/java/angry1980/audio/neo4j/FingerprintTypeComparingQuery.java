package angry1980.audio.neo4j;

import angry1980.audio.model.FingerprintType;
import angry1980.audio.stats.FingerprintTypeComparing;
import angry1980.audio.stats.ImmutableFingerprintTypeComparing;
import angry1980.neo4j.Query;
import com.google.common.collect.ImmutableMap;
import org.neo4j.graphdb.Result;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FingerprintTypeComparingQuery implements Query<FingerprintTypeComparingQuery>{

    //private static final String QUERY = "match (track1:TRACK)-[similar1:SIMILAR{type:{type1}}]->(track2:TRACK)"
    private static final String QUERY = "match (cluster1)<-[:IS]-(track1:TRACK)-[similar1:SIMILAR{type:{type1}}]->(track2:TRACK)-[:IS]->(cluster1)"
        + " where similar1.weight > {minWeight1}"
        + " optional match (track1)-[similar2:SIMILAR{type:{type2}}]->(track2)"
        + " where similar2.weight > {minWeight2}"
        + " with similar1.weight as weight, (similar2 is null) as empty, track1 as track"
        + " return count(distinct(track)) as result, min(weight) as minValue, not(empty) as common "
        ;

    private final FingerprintType type1;
    private final FingerprintType type2;
    private Map<FingerprintType, Integer> minWeights;
    private int minValue;
    private int commonCount;

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
        Map<Boolean, Record> map = Query.asStream(result)
                .map(data -> new Record(
                                    Query.getBooleanValue(data, "common").orElse(true),
                                    Query.getIntValue(data, "result").orElse(0),
                                    Query.getIntValue(data, "minValue").orElse(0)
                )).collect(Collectors.toMap(Record::isCommon, Function.identity()));
        Record r = map.get(true);
        if(r != null){
            this.minValue = r.getMin();
            this.commonCount = r.getCount();
        }
        return this;
    }

    public FingerprintTypeComparing merge(FingerprintTypeComparingQuery other){
        if(!type1.equals(other.type2)
                || !type2.equals(other.type1)
                || commonCount != this.commonCount){
            throw new IllegalArgumentException();
        }
        return ImmutableFingerprintTypeComparing.builder()
                .type1(type1)
                .type2(type2)
                .minWeightInCommon1(minValue)
                .minWeightInCommon2(other.minValue)
                .common(this.commonCount)
                .build();
    }

    private class Record{
        private boolean common;
        private int count;
        private int min;

        public Record(boolean common, int count, int min) {
            this.common = common;
            this.count = count;
            this.min = min;
        }

        public boolean isCommon() {
            return common;
        }

        public int getCount() {
            return count;
        }

        public int getMin() {
            return min;
        }
    }
}
