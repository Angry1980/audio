package angry1980.audio.dao;

import angry1980.audio.model.TrackHash;

import java.util.*;

public interface TrackHashDAO {

    Optional<TrackHash> create(TrackHash hash);

    Collection<TrackHash> findByHash(long hash);

    Map<Long, SortedSet<TrackHash>> findByHashesAndSortByTrack(Collection<TrackHash> hashes);

    Map<Long, SortedSet<TrackHash>> findByHashesAndSortByTrack(long[] hashes);
}
