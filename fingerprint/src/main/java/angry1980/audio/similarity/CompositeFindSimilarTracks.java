package angry1980.audio.similarity;

import angry1980.audio.model.ComparingType;
import angry1980.audio.model.Track;
import angry1980.audio.model.TrackSimilarity;
import rx.Observable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class CompositeFindSimilarTracks implements FindSimilarTracks{

    private FindSimilarTracks _default;
    private Optional<List<FindSimilarTracks>> findSimilarTracks;

    public CompositeFindSimilarTracks(Optional<List<FindSimilarTracks>> findSimilarTracks) {
        this.findSimilarTracks = Objects.requireNonNull(findSimilarTracks);
        this._default = new FindSimilarTracksFakeImpl();
    }

    @Override
    public Observable<TrackSimilarity> apply(Track track, ComparingType type) {
        return findSimilarTracks
                .flatMap(list -> list.stream()
                                    .filter(handler -> handler.test(type.getFingerprintType()))
                                    .findAny()
                ).orElse(_default)
                .apply(track, type);
    }
}
