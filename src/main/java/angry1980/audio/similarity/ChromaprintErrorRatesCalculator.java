package angry1980.audio.similarity;

import angry1980.audio.dao.FingerprintDAO;
import angry1980.audio.model.ChromaprintFingerprint;
import angry1980.audio.model.FingerprintType;
import angry1980.audio.model.TrackSimilarity;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ChromaprintErrorRatesCalculator implements Calculator<ChromaprintFingerprint> {

    private static final double positiveLimit = 0.8;

    private FingerprintDAO<ChromaprintFingerprint> fingerprintDAO;
    private int batchSize;
    private int errorLimit;

    public ChromaprintErrorRatesCalculator(FingerprintDAO<ChromaprintFingerprint> fingerprintDAO){
        this(fingerprintDAO, 25, 8);
    }

    public ChromaprintErrorRatesCalculator(FingerprintDAO<ChromaprintFingerprint> fingerprintDAO, int batchSize, int errorLimit) {
        this.fingerprintDAO = Objects.requireNonNull(fingerprintDAO);
        this.batchSize = batchSize;
        this.errorLimit = errorLimit;
    }

    @Override
    public List<TrackSimilarity> calculate(ChromaprintFingerprint fingerprint) {
        return fingerprintDAO.getAll().stream()
                    .filter(fp -> fp.getTrackId() != fingerprint.getTrackId())
                    .map(fp -> new TrackSimilarity(
                            fingerprint.getTrackId(),
                            fp.getTrackId(),
                            calculate(fingerprint.getHashes(), fp.getHashes()),
                            FingerprintType.CHROMAPRINT)
                    ).filter(ts -> ts.getValue() > 20)
                    .collect(Collectors.toList())
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
