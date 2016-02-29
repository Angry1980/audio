package angry1980.audio.similarity;

import angry1980.audio.model.FingerprintType;
import angry1980.audio.model.TrackSimilarity;

import java.util.Objects;
import java.util.Optional;

public class HashErrorRatesCalculatorSimilarTrackSource implements HashErrorRatesCalculatorTrackSource{

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
        return Optional.of(
                findSimilarTracks.apply(sourceTrackId, fingerprintType).stream()
                    .filter(ts -> ts.getValue() > limit)
                    .mapToLong(TrackSimilarity::getTrack2)
                    .toArray()
        );
    }
}
