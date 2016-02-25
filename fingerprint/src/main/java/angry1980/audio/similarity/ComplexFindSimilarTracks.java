package angry1980.audio.similarity;

import angry1980.audio.model.FingerprintType;
import angry1980.audio.model.TrackSimilarity;

import java.util.List;
import java.util.Objects;

public class ComplexFindSimilarTracks implements FindSimilarTracks{

    private FindSimilarTracks _default;
    private List<FindSimilarTracks> findSimilarTracks;

    public ComplexFindSimilarTracks(List<FindSimilarTracks> findSimilarTracks) {
        this.findSimilarTracks = Objects.requireNonNull(findSimilarTracks);
        this._default = new FindSimilarTracksFakeImpl();
    }

    @Override
    public List<TrackSimilarity> apply(long track, FingerprintType type) {
        return findSimilarTracks.stream()
                .filter(handler -> handler.test(type))
                .findAny()
                .orElse(_default)
                .apply(track, type);
    }
}
