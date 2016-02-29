package angry1980.audio.similarity;

import angry1980.audio.dao.FingerprintDAO;
import angry1980.audio.dao.TrackDAO;
import angry1980.audio.model.*;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class HashErrorRatesCalculator implements Calculator<Fingerprint> {

    private static Logger LOG = LoggerFactory.getLogger(HashErrorRatesCalculator.class);

    private static final double positiveLimit = 0.8;

    private FingerprintType type;
    private HashErrorRatesCalculatorTrackSource trackSource;
    private FingerprintDAO<Fingerprint> fingerprintDAO;
    private int batchSize;
    private int errorLimit;

    public HashErrorRatesCalculator(FingerprintType type, HashErrorRatesCalculatorTrackSource trackSource, FingerprintDAO<Fingerprint> fingerprintDAO){
        this(type, trackSource, fingerprintDAO, 25, 8);
    }

    public HashErrorRatesCalculator(FingerprintType type,
                                    HashErrorRatesCalculatorTrackSource trackSource,
                                    FingerprintDAO<Fingerprint> fingerprintDAO,
                                    int batchSize,
                                    int errorLimit) {
        this.type = type;
        this.trackSource = Objects.requireNonNull(trackSource);
        this.fingerprintDAO = Objects.requireNonNull(fingerprintDAO);
        this.batchSize = batchSize;
        this.errorLimit = errorLimit;
    }

    @Override
    public List<TrackSimilarity> calculate(Fingerprint fingerprint) {
        return trackSource.get(fingerprint.getTrackId())
                .map(fingerprintDAO::findByTrackIds)
                .map(list -> list.stream()
                    .map(fp -> (TrackSimilarity)ImmutableTrackSimilarity.builder()
                            .track1(fingerprint.getTrackId())
                            .track2(fp.getTrackId())
                            .value(calculate(fingerprint.getHashes(), fp.getHashes()))
                            .fingerprintType(type).build()
                    ).filter(ts -> ts.getValue() > 20)
                    .collect(Collectors.toList())
                ).orElseGet(() -> Collections.emptyList())
        ;
    }

    private int calculate(List<TrackHash> source, List<TrackHash> other) {
        int counter = 0;
        if(other == null || source == null){
            return counter;
        }
        int batchSize = Math.min(this.batchSize, other.size() - 1);
        for (int begin = 0; begin < source.size(); begin += batchSize){
            int end = Math.min(begin + batchSize, source.size());
            List<TrackHash> batch = source.subList(begin, end);
            int limit = (int) (batch.size() * positiveLimit);
            int s = 0;
            for(int i = 0; i < other.size() - batch.size(); i++){
                s = Math.max(s, check(batch, other.subList(i, i + batch.size())));
                if(s == batch.size()){
                    break;
                }
            }
            if(s > limit){
                counter += s;
            }
        }
        return counter;
    }


    private int check(List<TrackHash> source, List<TrackHash> other){
        int counter = 0;
        for(int i = 0; i < source.size() && i < other.size(); i++){
            int errors = Integer.bitCount(((int)source.get(i).getHash()) ^ ((int)other.get(i).getHash()));
            if(errors <= errorLimit){
                counter++;
            }
        }
        return counter;
    }

}
