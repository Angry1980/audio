package angry1980.audio.dao;

import angry1980.audio.model.TrackHash;

import java.util.*;

public interface TrackHashDAO {

    Optional<TrackHash> create(TrackHash hash);

    Collection<TrackHash> findByHash(long hash);

    Collection<TrackHash> findByHashes(long[] hashes);
}
