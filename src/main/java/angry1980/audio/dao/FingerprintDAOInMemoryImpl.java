package angry1980.audio.dao;

import angry1980.audio.model.Fingerprint;

import java.util.*;
import java.util.stream.Collectors;

public class FingerprintDAOInMemoryImpl<F extends Fingerprint> implements FingerprintDAO<F>{

    private Map<Long, F> fingerprints;

    public FingerprintDAOInMemoryImpl(){
        this.fingerprints = new HashMap<>();
    }

    @Override
    public Collection<F> getAll() {
        return fingerprints.values();
    }

    @Override
    public Optional<F> findByTrackId(long trackId) {
        return Optional.ofNullable(fingerprints.get(trackId));
    }

    @Override
    public Collection<F> findByTrackIds(long[] trackIds) {
        return Arrays.stream(trackIds)
                .mapToObj(this::findByTrackId)
                .filter(o -> o.isPresent())
                .map(o -> o.get())
                .collect(Collectors.toList());
    }

    @Override
    public Optional<F> create(F fingerprint) {
        fingerprints.put(fingerprint.getTrackId(), fingerprint);
        return Optional.of(fingerprint);
    }
}
