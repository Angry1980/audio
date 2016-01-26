package angry1980.audio.neo4j;

import angry1980.audio.model.FingerprintType;
import angry1980.neo4j.Query;
import com.google.common.collect.ImmutableMap;
import org.neo4j.graphdb.Result;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class FingerprintTypeQuery implements Query<Map<Boolean, Integer>>{

    private FingerprintType type;

    public FingerprintTypeQuery(FingerprintType type) {
        this.type = Objects.requireNonNull(type);
    }

    @Override
    public Map<String, Object> getParams() {
        return ImmutableMap.of("fingerprintType", type.name());
    }

    @Override
    public Map<Boolean, Integer> handle(Result result) {
        return Query.asStream(result).map(data -> new Record(
                        Boolean.parseBoolean(data.getOrDefault("r", "true").toString()),
                        Integer.decode(data.getOrDefault("result", "0").toString()))
        ).collect(Collectors.toMap(Record::isTruth, Record::getValue));

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
