package angry1980.audio.similarity;

import angry1980.audio.model.FingerprintType;
import angry1980.audio.model.TrackSimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

public class HashErrorRatesCalculatorSimilarTrackSource implements HashErrorRatesCalculatorTrackSource{

    private static Logger LOG = LoggerFactory.getLogger(HashErrorRatesCalculatorSimilarTrackSource.class);

    private FingerprintType fingerprintType;
    private FindSimilarTracks findSimilarTracks;
    private int limit;

    public HashErrorRatesCalculatorSimilarTrackSource(FingerprintType type, FindSimilarTracks findSimilarTracks) {
        this.fingerprintType = Objects.requireNonNull(type);
        this.findSimilarTracks = Objects.requireNonNull(findSimilarTracks);
        this.limit = 1;
    }

    public HashErrorRatesCalculatorSimilarTrackSource setLimit(int limit) {
        this.limit = limit;
        return this;
    }

    @Override
    public Optional<long[]> get(long sourceTrackId) {
        long[] result = findSimilarTracks.apply(sourceTrackId, fingerprintType).stream()
                            .filter(ts -> ts.getValue() > limit)
                            .mapToLong(TrackSimilarity::getTrack2)
                            .toArray();
        LOG.debug("There are {} tracks which are looking similar to {} by {}", new Object[]{result.length, sourceTrackId, fingerprintType});
        return Optional.of(result);
    }
}
