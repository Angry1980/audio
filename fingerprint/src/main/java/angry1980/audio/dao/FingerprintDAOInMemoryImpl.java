package angry1980.audio.dao;

import angry1980.audio.model.Fingerprint;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.*;
import java.util.stream.Collectors;

public class FingerprintDAOInMemoryImpl<F extends Fingerprint> implements FingerprintDAO<F>{

    private Long2ObjectMap<F> fingerprints;

    public FingerprintDAOInMemoryImpl(){
        this(16);
    }

    public FingerprintDAOInMemoryImpl(int expectedSize){
        this.fingerprints = new Long2ObjectOpenHashMap<>(expectedSize);
    }

    @Override
    public Collection<F> getAll() {
        return fingerprints.values();
    }

    @Override
    public F tryToFindByTrackId(long trackId) {
        return fingerprints.get(trackId);
    }

    @Override
    public Collection<F> findByTrackIds(long[] trackIds) {
        return Arrays.stream(trackIds)
                .mapToObj(this::tryToFindByTrackId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public F tryToCreate(F fingerprint) {
        fingerprints.put(fingerprint.getTrackId(), fingerprint);
        return fingerprint;
    }

}
