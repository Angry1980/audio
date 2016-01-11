package angry1980.audio.dao;

import angry1980.audio.model.Fingerprint;

import java.util.Collection;
import java.util.Optional;

public interface FingerprintDAO<F extends Fingerprint> {

    Collection<F> getAll();

    Optional<F> findByTrackId(long trackId);

    Optional<F> create(F fingerprint);
}
