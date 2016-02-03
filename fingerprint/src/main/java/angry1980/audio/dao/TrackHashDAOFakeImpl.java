package angry1980.audio.dao;

import angry1980.audio.model.TrackHash;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TrackHashDAOFakeImpl implements TrackHashDAO {
    @Override
    public Optional<TrackHash> create(TrackHash hash) {
        return Optional.of(hash);
    }

    @Override
    public List<TrackHash> findByHash(int hash) {
        return Collections.emptyList();
    }
}
