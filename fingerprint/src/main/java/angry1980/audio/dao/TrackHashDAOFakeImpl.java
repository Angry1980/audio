package angry1980.audio.dao;

import angry1980.audio.model.TrackHash;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.*;

public class TrackHashDAOFakeImpl implements TrackHashDAO {
    @Override
    public Optional<TrackHash> create(TrackHash hash) {
        return Optional.of(hash);
    }

    @Override
    public List<TrackHash> findByHash(long hash) {
        return Collections.emptyList();
    }

    @Override
    public Long2ObjectMap<SortedSet<TrackHash>> findByHashesAndSortByTrack(Collection<TrackHash> hashes) {
        return new Long2ObjectOpenHashMap<>();
    }

    @Override
    public Long2ObjectMap<SortedSet<TrackHash>> findByHashesAndSortByTrack(long[] hashes) {
        return new Long2ObjectOpenHashMap<>();
    }
}
