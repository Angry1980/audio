package angry1980.audio.dao;

import angry1980.audio.model.Fingerprint;

import java.util.Collection;
import java.util.Optional;

public interface FingerprintDAO<F extends Fingerprint> {

    Collection<F> getAll();

    Collection<F> findByTrackIds(long[] trackIds);

    default Optional<F> findByTrackId(long trackId){
        return Optional.ofNullable(tryToFindByTrackId(trackId));
    }

    F tryToFindByTrackId(long trackId);

    default Optional<F> create(F fingerprint){
        return Optional.of(tryToCreate(fingerprint));
    }

    F tryToCreate(F fingerprint);
}
