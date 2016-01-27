package angry1980.audio.neo4j;

import angry1980.audio.model.FingerprintType;
import angry1980.audio.stats.FingerprintTypeComparing;
import angry1980.audio.stats.ImmutableFingerprintTypeComparing;
import angry1980.neo4j.Query;
import com.google.common.collect.ImmutableMap;
import org.neo4j.graphdb.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

public class FingerprintTypeComparingQuery implements Query<FingerprintTypeComparingQuery>{

    private static Logger LOG = LoggerFactory.getLogger(FingerprintTypeComparingQuery.class);

    private static final String QUERY = "match (cluster1)<-[:IS]-(track1:TRACK)-[similar1:SIMILAR{type:{type1}}]->(track2:TRACK)-[:IS]->(cluster1)"
            + " optional match (track1)-[similar2:SIMILAR{type:{type2}}]->(track2)"
            + " with similar1.weight as weight, (similar2.id is null) as notempty"
            + " return count(weight) as result, min(weight) as minValue, notempty as common "
            ;

    private final FingerprintType type1;
    private final FingerprintType type2;

    public FingerprintTypeComparingQuery(FingerprintType type1, FingerprintType type2) {
        this.type1 = Objects.requireNonNull(type1);
        this.type2 = Objects.requireNonNull(type2);
    }

    @Override
    public String getQuery() {
        return QUERY;
    }

    @Override
    public Map<String, Object> getParams() {
        return ImmutableMap.of("type1", type1, "type2", type2);
    }

    @Override
    public FingerprintTypeComparingQuery handle(Result result) {
        LOG.info(type1 + "-" + type2 + ":");
        LOG.info(result.resultAsString());
        return this;
    }

    public FingerprintTypeComparing merge(FingerprintTypeComparingQuery other){
        //todo: check types
        return ImmutableFingerprintTypeComparing.builder()
                .type1(type1)
                .type2(type2)
                .minWeightInCommon1(0)
                .minWeightInCommon2(0)
                .common(0)
                .all(0)
                .build();
    }

    public FingerprintType getType1() {
        return type1;
    }

    public FingerprintType getType2() {
        return type2;
    }
}
