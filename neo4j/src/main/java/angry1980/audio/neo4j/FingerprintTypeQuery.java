package angry1980.audio.neo4j;

import angry1980.audio.model.FingerprintType;
import angry1980.neo4j.Query;
import com.google.common.collect.ImmutableMap;
import org.neo4j.graphdb.Result;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class FingerprintTypeQuery implements Query<FingerprintTypeQuery>{

    private final FingerprintType type;
    private final int minWeight;
    private Map<Boolean, Integer> result;

    public FingerprintTypeQuery(FingerprintType type) {
        this(type, 1);
    }

    public FingerprintTypeQuery(FingerprintType type, int minWeight) {
        this.minWeight = minWeight;
        this.type = Objects.requireNonNull(type);
    }

    @Override
    public Map<String, Object> getParams() {
        return ImmutableMap.of("fingerprintType", type.name(), "minWeight", minWeight);
    }

    @Override
    public FingerprintTypeQuery handle(Result result) {
        this.result = Query.asStream(result).map(data -> new Record(
                Query.getBooleanValue(data, "r").orElse(true),
                Query.getIntValue(data, "result").orElse(0)
        )).collect(Collectors.toMap(Record::isTruth, Record::getValue));
        return this;

    }

    public Map<Boolean, Integer> getValues() {
        return result;
    }

    public int getValue(boolean key){
        return result.getOrDefault(key, 0);
    }

    private class Record{
        private boolean truth;
        private int value;

        public Record(boolean truth, int value) {
            this.truth = truth;
            this.value = value;
        }

        public boolean isTruth() {
            return truth;
        }

        public int getValue() {
            return value;
        }
    }

}
