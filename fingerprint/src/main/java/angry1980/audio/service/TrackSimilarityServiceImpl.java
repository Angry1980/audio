package angry1980.audio.service;

import angry1980.audio.dao.TrackDAO;
import angry1980.audio.dao.TrackSimilarityDAO;
import angry1980.audio.model.FingerprintType;
import angry1980.audio.model.Track;
import angry1980.audio.similarity.FindSimilarTracks;
import angry1980.audio.similarity.TrackSimilarities;
import angry1980.audio.similarity.TracksToCalculate;
import angry1980.utils.ImmutableCollectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.List;
import java.util.Objects;

public class TrackSimilarityServiceImpl implements TrackSimilarityService {

    private static Logger LOG = LoggerFactory.getLogger(TrackSimilarityServiceImpl.class);

    private TrackDAO trackDAO;
    private TrackSimilarityDAO trackSimilarityDAO;
    private List<FindSimilarTracks> findSimilarTracks;
    private TracksToCalculate tracksToCalculate;

    public TrackSimilarityServiceImpl(TrackDAO trackDAO,
                                      TrackSimilarityDAO trackSimilarityDAO,
                                      List<FindSimilarTracks> findSimilarTracks,
                                      TracksToCalculate tracksToCalculate) {
        this.trackDAO = Objects.requireNonNull(trackDAO);
        this.trackSimilarityDAO = Objects.requireNonNull(trackSimilarityDAO);
        this.findSimilarTracks = Objects.requireNonNull(findSimilarTracks);
        this.tracksToCalculate = Objects.requireNonNull(tracksToCalculate);
    }

    @Override
    public Observable<Track> getTracksToCalculateSimilarity() {
        return tracksToCalculate.get();
    }

    @Override
    public Observable<TrackSimilarities> findOrCalculateSimilarities(Track track) {
        return Observable.just(
                    findSimilarTracks.stream()
                        .peek(handler -> LOG.debug("{} is getting ready to handle by {}", track.getId(), handler))
                        .flatMap(handler -> handler.apply(track.getId()).stream())
                        .collect(ImmutableCollectors.toSet())
                ).map(s -> {
                        LOG.debug("{} was handled. There are {} similarities. ", track.getId(), s.size());
                        return new TrackSimilarities(track, s);
                });
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
