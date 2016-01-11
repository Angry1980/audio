package angry1980.audio.dao;

import angry1980.audio.model.Fingerprint;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public class FingerprintDAOFakeImpl implements FingerprintDAO<Fingerprint>{

    @Override
    public Collection<Fingerprint> getAll() {
        return Collections.emptyList();
    }

    @Override
    public Optional<Fingerprint> findByTrackId(long trackId) {
        return Optional.empty();
    }

    @Override
    public Optional<Fingerprint> create(Fingerprint fingerprint) {
        return Optional.of(fingerprint);
    }
}
