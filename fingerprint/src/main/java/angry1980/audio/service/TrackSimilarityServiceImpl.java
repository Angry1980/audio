package angry1980.audio.service;

import angry1980.audio.dao.TrackDAO;
import angry1980.audio.dao.TrackSimilarityDAO;
import angry1980.audio.model.FingerprintType;
import angry1980.audio.model.Track;
import angry1980.audio.similarity.FindSimilarTracks;
import angry1980.audio.similarity.TrackSimilarities;
import angry1980.utils.ImmutableCollectors;
import rx.Observable;

import java.util.List;

public class TrackSimilarityServiceImpl implements TrackSimilarityService {

    private TrackDAO trackDAO;
    private TrackSimilarityDAO trackSimilarityDAO;
    private List<FindSimilarTracks> findSimilarTracks;

    public TrackSimilarityServiceImpl(TrackDAO trackDAO,
                                      TrackSimilarityDAO trackSimilarityDAO,
                                      List<FindSimilarTracks> findSimilarTracks) {
        this.trackDAO = trackDAO;
        this.trackSimilarityDAO = trackSimilarityDAO;
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

    @Override
    public Observable<TrackSimilarities> getReport() {
        return Observable.create(subscriber -> {
            trackDAO.getAllOrEmpty().stream()
                    .map(track -> new TrackSimilarities(track, trackSimilarityDAO.findByTrackIdOrEmpty(track.getId())))
                    .forEach(subscriber::onNext);
            subscriber.onCompleted();
        });
    }


}
