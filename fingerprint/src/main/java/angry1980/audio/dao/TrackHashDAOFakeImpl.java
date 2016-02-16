package angry1980.audio.dao;

import angry1980.audio.model.TrackHash;

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
    public Map<Long, SortedSet<TrackHash>> findByHashesAndSortByTrack(Collection<TrackHash> hashes) {
        return Collections.emptyMap();
    }

    @Override
    public Map<Long, SortedSet<TrackHash>> findByHashesAndSortByTrack(long[] hashes) {
        return Collections.emptyMap();
    }
}
