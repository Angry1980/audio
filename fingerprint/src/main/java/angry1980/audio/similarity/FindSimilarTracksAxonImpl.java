package angry1980.audio.similarity;

import angry1980.audio.dao.TrackSimilarityDAO;
import angry1980.audio.model.ComparingType;
import angry1980.audio.model.FingerprintType;
import angry1980.audio.model.Track;
import angry1980.audio.model.TrackSimilarity;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.Objects;

public class FindSimilarTracksAxonImpl implements FindSimilarTracks {

    private static Logger LOG = LoggerFactory.getLogger(FindSimilarTracksImpl.class);

    private TrackSimilarityDAO trackSimilarityDAO;
    private FingerprintType fingerprintType;
    private CommandGateway commandGateway;
    private EventBus eventBus;

    public FindSimilarTracksAxonImpl(TrackSimilarityDAO trackSimilarityDAO,
                                     FingerprintType fingerprintType,
                                     CommandGateway commandGateway,
                                     EventBus eventBus) {
        this.trackSimilarityDAO = Objects.requireNonNull(trackSimilarityDAO);
        this.fingerprintType = Objects.requireNonNull(fingerprintType);
        this.commandGateway = Objects.requireNonNull(commandGateway);
        this.eventBus = Objects.requireNonNull(eventBus);
    }

    @Override
    public boolean test(FingerprintType fingerprintType) {
        return this.fingerprintType.equals(fingerprintType);
    }

    @Override
    public Observable<TrackSimilarity> apply(Track track, ComparingType type) {
        return trackSimilarityDAO.findByTrackIdAndFingerprintType(track.getId(), type)
                .map(Observable::from)
                .orElseGet(() -> Observable.create(
                        TrackSimilarityEventListenerBuilder.create(track, type).build(eventBus, commandGateway)
                ));
    }

    @Override
    public String toString() {
        return "FindSimilarTracks{" +
                "fingerprintType=" + fingerprintType +
                '}';
    }
}
