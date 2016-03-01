package angry1980.audio.similarity;

import angry1980.audio.dao.FingerprintDAO;
import angry1980.audio.model.*;
import angry1980.utils.Numbered;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class HashErrorRatesCalculator implements Calculator<Fingerprint> {

    private static Logger LOG = LoggerFactory.getLogger(HashErrorRatesCalculator.class);

    private static final double positiveLimit = 0.8;

    private ComparingType type;
    private HashErrorRatesCalculatorTrackSource trackSource;
    private FingerprintDAO<Fingerprint> fingerprintDAO;
    private int batchSize;
    private int errorLimit;

    public HashErrorRatesCalculator(ComparingType type, HashErrorRatesCalculatorTrackSource trackSource, FingerprintDAO<Fingerprint> fingerprintDAO){
        this(type, trackSource, fingerprintDAO, 25, 8);
    }

    public HashErrorRatesCalculator(ComparingType type,
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
                .map(list -> calculate(fingerprint.getTrackId(), getHashes(fingerprint), list))
                .orElseGet(() -> Collections.emptyList())
        ;
    }

    private List<TrackSimilarity> calculate(long trackId, int[] source, Collection<Fingerprint> others){
        return others.stream()
                .map(fp -> new Numbered<>(fp.getTrackId(), calculate(source, getHashes(fp))))
                .filter(n -> n.getValue().compareTo(20) > 0)
                .map(n -> ImmutableTrackSimilarity.builder()
                        .track1(trackId)
                        .track2(n.getNumber())
                        .value(n.getValue())
                        .comparingType(type)
                        .build()
                ).collect(Collectors.toList());
    }

    private int[] getHashes(Fingerprint fingerprint){
        return fingerprint.getHashes().stream()
                .mapToInt(hash -> (int)hash.getHash())
                .toArray();
    }

    private int calculate(int[] source, int[] other) {
        int counter = 0;
        if(other == null
                || source == null){
            return counter;
        }
        int batchSize = Math.min(this.batchSize, other.length);
        int batchLength = batchSize;
        int limit = (int) (batchLength * positiveLimit);
        for (int begin = 0; begin < source.length; begin += batchSize){
            int end = begin + batchSize;
            if(end > source.length){
                //values for last batch
                end = source.length;
                batchLength = end - begin;
                limit = (int) (batchLength * positiveLimit);
            }
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
