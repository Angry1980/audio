package angry1980.audio.dao;

import angry1980.audio.model.Fingerprint;

import java.util.Collection;
import java.util.Collections;

public class FingerprintDAOFakeImpl implements FingerprintDAO<Fingerprint>{

    @Override
    public Collection<Fingerprint> getAll() {
        return Collections.emptyList();
    }

    @Override
    public Collection<Fingerprint> findByTrackIds(long[] trackIds) {
        return Collections.emptyList();
    }

    @Override
    public Fingerprint tryToFindByTrackId(long trackId) {
        return null;
    }

    @Override
    public Fingerprint tryToCreate(Fingerprint fingerprint) {
        return fingerprint;
    }
}
