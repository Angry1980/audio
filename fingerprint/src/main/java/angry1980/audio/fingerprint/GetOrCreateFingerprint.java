package angry1980.audio.fingerprint;

import angry1980.audio.model.ComparingType;
import angry1980.audio.model.Fingerprint;
import angry1980.audio.dao.FingerprintDAO;
import angry1980.audio.model.Track;

import java.util.Objects;

public class GetOrCreateFingerprint<F extends Fingerprint>{

    private FingerprintDAO<F> fingerprintDAO;
    private Calculator<F> fingerprintCalculator;
    private InvertedIndex<F> invertedIndex;

    public GetOrCreateFingerprint(FingerprintDAO<F> fingerprintDAO,
                                  Calculator<F> fingerprintCalculator,
                                  InvertedIndex<F> invertedIndex) {
        this.fingerprintDAO = Objects.requireNonNull(fingerprintDAO);
        this.fingerprintCalculator = Objects.requireNonNull(fingerprintCalculator);
        this.invertedIndex = Objects.requireNonNull(invertedIndex);
    }

    public F apply(Track track, ComparingType comparingType) {
        return fingerprintDAO.findByTrackId(track.getId())
                .orElseGet(() ->
                        fingerprintCalculator.calculate(track, comparingType.getFingerprintType())
                                .flatMap(fingerprintDAO::create)
                                .map(invertedIndex::save)
                                .orElse(null)
                );
    }
}
