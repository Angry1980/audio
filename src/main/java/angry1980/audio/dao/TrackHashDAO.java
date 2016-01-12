package angry1980.audio.dao;

import angry1980.audio.model.TrackHash;

import java.util.List;
import java.util.Optional;

public interface TrackHashDAO {

    Optional<TrackHash> create(TrackHash hash);

    List<TrackHash> findByHash(int hash);
}
