package angry1980.audio.dao;

import angry1980.audio.model.TrackHash;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;

public interface TrackHashDAO {

    Optional<TrackHash> create(TrackHash hash);

    Collection<TrackHash> findByHash(long hash);

    Long2ObjectMap<SortedSet<TrackHash>> findByHashesAndSortByTrack(Collection<TrackHash> hashes);

    Long2ObjectMap<SortedSet<TrackHash>> findByHashesAndSortByTrack(long[] hashes);
}
