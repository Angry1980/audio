package angry1980.audio.neo4j;

import angry1980.audio.model.FingerprintType;
import angry1980.neo4j.Query;
import org.neo4j.graphdb.Result;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FingerprintTypeComparingAllQuery implements Query<FingerprintTypeComparingAllQuery>{

    private static final String QUERY = "match (cluster1)<-[:IS]-(track1:TRACK)-[similar1:SIMILAR{type:{type1}}]->(track2:TRACK)-[:IS]->(cluster1)"
            + " optional match (track1)-[similar2:SIMILAR{type:{type2}}]->(track2)"
            + " optional match (track1)-[similar3:SIMILAR{type:{type3}}]->(track2)"
            + " where similar1.weight > {minWeight1} and similar2.weight > {minWeight2} and similar3.weight > {minWeight3}"
            + " with similar1.weight as weight, not((similar2.id is null) or (similar3.id is null)) as notempty"
            + " return count(weight) as result, notempty as common "
            ;

    private Map<FingerprintType, Integer> minWeights;
    private int value;

    public FingerprintTypeComparingAllQuery(Map<FingerprintType, Integer> minWeights) {
        this.minWeights = Objects.requireNonNull(minWeights);
    }

    @Override
    public String getQuery() {
        return QUERY;
    }

    public int getValue() {
        return value;
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
        this.value = Query.asStream(result)
                .filter(data -> Query.getBooleanValue(data, "common").orElse(false))
                .map(data -> Query.getIntValue(data, "result").orElse(0))
                .findAny().orElse(0);
        return this;

    }

}
