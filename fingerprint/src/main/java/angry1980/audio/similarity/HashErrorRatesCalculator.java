package angry1980.audio.similarity;

import angry1980.audio.dao.FingerprintDAO;
import angry1980.audio.dao.TrackDAO;
import angry1980.audio.model.HashFingerprint;
import angry1980.audio.model.FingerprintType;
import angry1980.audio.model.TrackSimilarity;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class HashErrorRatesCalculator implements Calculator<HashFingerprint> {

    private static final double positiveLimit = 0.8;

    private FingerprintType type;
    private TrackDAO trackDAO;
    private FingerprintDAO<HashFingerprint> fingerprintDAO;
    private int batchSize;
    private int errorLimit;

    public HashErrorRatesCalculator(FingerprintType type, TrackDAO trackDAO, FingerprintDAO<HashFingerprint> fingerprintDAO){
        this(type, trackDAO, fingerprintDAO, 25, 8);
    }

    public HashErrorRatesCalculator(FingerprintType type, TrackDAO trackDAO, FingerprintDAO<HashFingerprint> fingerprintDAO, int batchSize, int errorLimit) {
        this.type = type;
        this.trackDAO = Objects.requireNonNull(trackDAO);
        this.fingerprintDAO = Objects.requireNonNull(fingerprintDAO);
        this.batchSize = batchSize;
        this.errorLimit = errorLimit;
    }

    @Override
    public List<TrackSimilarity> calculate(HashFingerprint fingerprint) {
        return trackDAO.get(fingerprint.getTrackId())
                .map(track -> trackDAO.findByCluster(track.getCluster()).stream().mapToLong(t -> t.getId()).toArray())
                .map(fingerprintDAO::findByTrackIds)
                .map(list -> list.stream()
                    .filter(fp -> fp.getTrackId() != fingerprint.getTrackId())
                    .map(fp -> new TrackSimilarity(
                            fingerprint.getTrackId(),
                            fp.getTrackId(),
                            calculate(fingerprint.getHashes(), fp.getHashes()),
                            type)
                    ).filter(ts -> ts.getValue() > 20)
                    .collect(Collectors.toList())
                ).orElseGet(() -> Collections.emptyList())
        ;
    }

    private int calculate(int[] source, int[] other) {
        int counter = 0;
        if(other == null || source == null){
            return counter;
        }
        int batchSize = Math.min(this.batchSize, other.length - 1);
        for (int begin = 0; begin < source.length; begin += batchSize){
            int end = Math.min(begin + batchSize, source.length);
            int[] batch = ArrayUtils.subarray(source, begin, end);
            int limit = (int) (batch.length * positiveLimit);
            int s = 0;
            for(int i = 0; i < other.length - batch.length; i++){
                s = Math.max(s, check(batch, ArrayUtils.subarray(other, i, i + batch.length)));
                if(s == batch.length){
                    break;
                }
            }
            if(s > limit){
                counter += s;
            }
        }
        return counter;
    }


    private int check(int[] source, int[] other){
        int counter = 0;
        for(int i = 0; i < source.length && i < other.length; i++){
            int errors = Integer.bitCount(source[i] ^ other[i]);
            if(errors <= errorLimit){
                counter++;
            }
        }
        return counter;
    }

}
