package angry1980.audio.similarity;

import angry1980.audio.dao.TrackSimilarityDAO;
import angry1980.audio.fingerprint.GetOrCreateFingerprint;
import angry1980.audio.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.Objects;
import java.util.Optional;

public class FindSimilarTracksImpl implements FindSimilarTracks {

    private static Logger LOG = LoggerFactory.getLogger(FindSimilarTracksImpl.class);

    private TrackSimilarityDAO trackSimilarityDAO;
    private GetOrCreateFingerprint<Fingerprint> fingerprintHandler;
    private Calculator calculator;
    private FingerprintType fingerprintType;

    public FindSimilarTracksImpl(TrackSimilarityDAO trackSimilarityDAO,
                                 GetOrCreateFingerprint<Fingerprint> fingerprintHandler,
                                 Calculator calculator,
                                 FingerprintType fingerprintType) {
        this.trackSimilarityDAO = Objects.requireNonNull(trackSimilarityDAO);
        this.fingerprintHandler = Objects.requireNonNull(fingerprintHandler);
        this.calculator = Objects.requireNonNull(calculator);
        this.fingerprintType = Objects.requireNonNull(fingerprintType);
    }

    @Override
    public boolean test(FingerprintType fingerprintType) {
        return this.fingerprintType.equals(fingerprintType);
    }

    @Override
    public Observable<TrackSimilarity> apply(Track track, ComparingType type) {
        return trackSimilarityDAO.findByTrackIdAndFingerprintType(track.getId(), type)
                    .map(Observable::from)
                    .orElseGet(() -> calculate(track, type)
                                        //todo: use event bus
                                        .map(ts -> trackSimilarityDAO.create(ts))
                                        .filter(Optional::isPresent)
                                        .map(Optional::get)
                    );
    }

    private Observable<TrackSimilarity> calculate(Track track, ComparingType type){
        //todo: send command
        LOG.debug("Start calculation of {} similarities for track {}", type, track.getId());
        return Optional.ofNullable(fingerprintHandler.apply(track, type))
                .map(fingerprint -> calculator.calculate(fingerprint, type))
                .orElseGet(() -> {
                    LOG.debug("It's not possible to calculate {} similarities for track {}", type, track.getId());
                    return Observable.empty();
                });
    }

    @Override
    public String toString() {
        return "FindSimilarTracks{" +
                "fingerprintType=" + fingerprintType +
                '}';
    }
}
