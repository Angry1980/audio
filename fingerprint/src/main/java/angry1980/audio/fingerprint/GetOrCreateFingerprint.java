package angry1980.audio.fingerprint;

import angry1980.audio.model.ComparingType;
import angry1980.audio.model.Fingerprint;
import angry1980.audio.dao.FingerprintDAO;
import angry1980.audio.dao.TrackDAO;
import angry1980.audio.model.Track;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class GetOrCreateFingerprint<F extends Fingerprint>{

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

    public F apply(long trackId, ComparingType comparingType) {
        Function<Track, Optional<F>> f = track -> fingerprintCalculator.calculate(track, comparingType.getFingerprintType());
        return fingerprintDAO.findByTrackId(trackId)
                .orElseGet(() ->
                        trackDAO.get(trackId)
                                .flatMap(f)
                                .flatMap(fingerprintDAO::create)
                                .map(invertedIndex::save)
                                .orElse(null)
                );
    }
}
