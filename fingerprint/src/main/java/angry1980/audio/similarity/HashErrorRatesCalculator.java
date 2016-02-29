package angry1980.audio.similarity;

import angry1980.audio.dao.FingerprintDAO;
import angry1980.audio.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
        int[] source = fingerprint.getHashes().stream()
                        .mapToInt(hash -> (int)hash.getHash())
                        .toArray();
        return trackSource.get(fingerprint.getTrackId())
                .map(fingerprintDAO::findByTrackIds)
                .map(list -> list.stream()
                    .map(fp -> (TrackSimilarity)ImmutableTrackSimilarity.builder()
                            .track1(fingerprint.getTrackId())
                            .track2(fp.getTrackId())
                            .value(calculate(source, fp.getHashes().stream().mapToInt(hash -> (int)hash.getHash()).toArray()))
                            .fingerprintType(type)
                            .build()
                    ).filter(ts -> ts.getValue() > 20)
                    .collect(Collectors.toList())
                ).orElseGet(() -> Collections.emptyList())
        ;
    }

    private int calculate(int[] source, int[] other) {
        int counter = 0;
        if(other == null
                || source == null){
            return counter;
        }
        int batchSize = Math.min(this.batchSize, other.length - 1);
        for (int begin = 0; begin < source.length; begin += batchSize){
            int end = Math.min(begin + batchSize, source.length);
            int batchLength = end - begin;
            int limit = (int) (batchLength * positiveLimit);
            int s = 0;
            for(int i = 0; i < other.length - batchLength; i++){
                s = Math.max(s, check(source, begin, end, other, i, i + batchLength));
                if(s == batchLength){
                    break;
                }
            }
            if(s > limit){
                counter += s;
            }
        }
        return counter;
    }

    private int check(int[] source, int sourceStart, int sourceEnd,
                        int[] other, int otherStart, int otherEnd){
        int counter = 0;
        for(int i = sourceStart, j = otherStart; i < sourceEnd && j < otherEnd; i++, j++){
            int errors = Integer.bitCount(source[i] ^ other[j]);
            if(errors <= errorLimit){
                counter++;
            }
        }
        return counter;
    }

}
