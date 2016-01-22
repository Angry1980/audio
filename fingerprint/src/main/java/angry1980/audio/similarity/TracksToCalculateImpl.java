package angry1980.audio.similarity;

import angry1980.audio.dao.TrackDAO;
import angry1980.audio.model.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.Objects;

public class TracksToCalculateImpl implements TracksToCalculate {

    private static Logger LOG = LoggerFactory.getLogger(TracksToCalculateImpl.class);

    private TrackDAO trackDAO;

    public TracksToCalculateImpl(TrackDAO trackDAO) {
        this.trackDAO = Objects.requireNonNull(trackDAO);
    }

    @Override
    public Observable<Track> get() {
        return Observable.<Track>create(subscriber -> {
            trackDAO.getAllOrEmpty().stream()
                    .peek(track -> LOG.info("{} is ready for similarity calculation", track))
                    .forEach(subscriber::onNext);
            subscriber.onCompleted();
        })
        ;
    }
}
