package angry1980.audio.similarity;

import angry1980.audio.dao.FingerprintDAO;
import angry1980.audio.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.Objects;
import java.util.Optional;

public class HashErrorRatesCalculator implements Calculator<Fingerprint> {

    private static Logger LOG = LoggerFactory.getLogger(HashErrorRatesCalculator.class);

    private static final double positiveLimit = 0.8;

    private HashErrorRatesCalculatorTrackSource trackSource;
    private FingerprintDAO<Fingerprint> fingerprintDAO;
    private int batchSize;
    private int errorLimit;

    public HashErrorRatesCalculator(HashErrorRatesCalculatorTrackSource trackSource, FingerprintDAO<Fingerprint> fingerprintDAO){
        this(trackSource, fingerprintDAO, 25, 8);
    }

    public HashErrorRatesCalculator(HashErrorRatesCalculatorTrackSource trackSource,
                                    FingerprintDAO<Fingerprint> fingerprintDAO,
                                    int batchSize,
                                    int errorLimit) {
        this.trackSource = Objects.requireNonNull(trackSource);
        this.fingerprintDAO = Objects.requireNonNull(fingerprintDAO);
        this.batchSize = batchSize;
        this.errorLimit = errorLimit;
    }

    @Override
    public boolean test(SimilarityType similarityType) {
        return SimilarityType.ERROR_RATE.equals(similarityType);
    }

    @Override
    public Observable<TrackSimilarity> calculate(Fingerprint fingerprint, ComparingType comparingType) {
        return trackSource.get(fingerprint.getTrackId())
                .map(trackId -> fingerprintDAO.findByTrackId(trackId)
                                    .flatMap(f -> calculate(fingerprint.getTrackId(), getHashes(fingerprint), comparingType, f))
                ).filter(Optional::isPresent)
                .map(Optional::get)
        ;
    }

    private Optional<TrackSimilarity> calculate(long trackId, int[] source, ComparingType comparingType, Fingerprint fp){
        return Optional.of(getHashes(fp))
                .map(hashes -> calculate(source, hashes))
                .filter(value -> value.compareTo(20) > 0)
                .map(value -> ImmutableTrackSimilarity.builder()
                        .track1(trackId)
                        .track2(fp.getTrackId())
                        .value(value)
                        .comparingType(comparingType)
                        .build()
        );
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
