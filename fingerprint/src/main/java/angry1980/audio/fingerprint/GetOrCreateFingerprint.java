package angry1980.audio.fingerprint;

import angry1980.audio.model.Fingerprint;
import angry1980.audio.dao.FingerprintDAO;
import angry1980.audio.dao.TrackDAO;

import java.util.Objects;
import java.util.function.LongFunction;

public class GetOrCreateFingerprint<F extends Fingerprint> implements LongFunction<F>{

    private FingerprintDAO<F> fingerprintDAO;
    private TrackDAO trackDAO;
    private Calculator<F> fingerprintCalculator;
    private InvertedIndex<F> invertedIndex;

    public GetOrCreateFingerprint(FingerprintDAO<F> fingerprintDAO,
                                  TrackDAO trackDAO,
                                  Calculator<F> fingerprintCalculator,
                                  InvertedIndex<F> invertedIndex) {
        this.fingerprintDAO = Objects.requireNonNull(fingerprintDAO);
        this.trackDAO = Objects.requireNonNull(trackDAO);
        this.fingerprintCalculator = Objects.requireNonNull(fingerprintCalculator);
        this.invertedIndex = Objects.requireNonNull(invertedIndex);
    }

    @Override
    public F apply(long trackId) {
        return fingerprintDAO.findByTrackId(trackId)
                .orElseGet(() ->
                        trackDAO.get(trackId)
                                .flatMap(fingerprintCalculator::calculate)
                                .flatMap(fingerprintDAO::create)
                                .map(invertedIndex::save)
                                .orElse(null)
                );
    }
}
