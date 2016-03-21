package angry1980.audio.similarity;

import angry1980.audio.dao.TrackSimilarityDAO;
import angry1980.audio.fingerprint.GetOrCreateFingerprint;
import angry1980.audio.model.ComparingType;
import angry1980.audio.model.Fingerprint;
import angry1980.audio.model.FingerprintType;
import angry1980.audio.model.TrackSimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public List<TrackSimilarity> apply(long trackId, ComparingType type) {
        return trackSimilarityDAO.findByTrackIdAndFingerprintType(trackId, type)
                    .orElseGet(() -> calculate(trackId, type).stream()
                                        .map(trackSimilarityDAO::create)
                                        .filter(Optional::isPresent)
                                        .map(Optional::get)
                                        .collect(Collectors.toList())
                    );
    }

    private List<TrackSimilarity> calculate(long trackId, ComparingType type){
        LOG.debug("Start calculation of {} similarities for track {}", type, trackId);
        return Optional.ofNullable(fingerprintHandler.apply(trackId, type))
                .map(fingerprint -> calculator.calculate(fingerprint, type))
                .orElseGet(() -> {
                    LOG.debug("It's not possible to calculate {} similarities for track {}", type, trackId);
                    return Collections.<TrackSimilarity>emptyList();
                });
    }

    @Override
    public String toString() {
        return "FindSimilarTracks{" +
                "fingerprintType=" + fingerprintType +
                '}';
    }
}
