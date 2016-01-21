package angry1980.audio.similarity;

import angry1980.audio.dao.TrackSimilarityDAO;
import angry1980.audio.model.Fingerprint;
import angry1980.audio.model.FingerprintType;
import angry1980.audio.model.TrackSimilarity;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.LongFunction;
import java.util.stream.Collectors;

public class FindSimilarTracksImpl implements FindSimilarTracks {

    private TrackSimilarityDAO trackSimilarityDAO;
    private LongFunction<Fingerprint> fingerprintHandler;
    private Calculator calculator;
    private FingerprintType fingerprintType;

    public FindSimilarTracksImpl(TrackSimilarityDAO trackSimilarityDAO,
                                 LongFunction<Fingerprint> fingerprintHandler,
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
    public List<TrackSimilarity> apply(long trackId) {
        return trackSimilarityDAO.findByTrackIdAndFingerprintType(trackId, fingerprintType)
                    .orElseGet(() -> calculate(trackId).stream()
                                        .map(trackSimilarityDAO::create)
                                        .map(Optional::get)
                                        .collect(Collectors.toList())
                    );
    }

    private List<TrackSimilarity> calculate(long trackId){
        return Optional.of(trackId)
                .map(fingerprintHandler::apply)
                .map(fingerprint -> calculator.calculate(fingerprint))
                .orElseGet(() -> Collections.<TrackSimilarity>emptyList())
        ;
    }

    @Override
    public String toString() {
        return "FindSimilarTracks{" +
                "fingerprintType=" + fingerprintType +
                '}';
    }
}
