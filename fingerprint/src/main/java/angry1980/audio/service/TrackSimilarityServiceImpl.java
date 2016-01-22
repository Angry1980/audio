package angry1980.audio.service;

import angry1980.audio.model.FingerprintType;
import angry1980.audio.model.Track;
import angry1980.audio.similarity.FindSimilarTracks;
import angry1980.utils.ImmutableCollectors;
import rx.Observable;

import java.util.List;

public class TrackSimilarityServiceImpl implements TrackSimilarityService {

    private List<FindSimilarTracks> findSimilarTracks;

    public TrackSimilarityServiceImpl(List<FindSimilarTracks> findSimilarTracks) {
        this.findSimilarTracks = findSimilarTracks;
    }

    @Override
    public Observable<TrackSimilarities> findOrCalculateSimilarities(Track track) {
        return Observable.just(
                    findSimilarTracks.stream()
                        .flatMap(handler -> handler.apply(track.getId()).stream())
                        .collect(ImmutableCollectors.toSet())
                ).map(s -> new TrackSimilarities(track, s))
        ;
    }

    @Override
    public Observable<TrackSimilarities> findOrCalculateSimilarities(Track track, FingerprintType fingerprintType) {
        throw new UnsupportedOperationException();
    }
}
