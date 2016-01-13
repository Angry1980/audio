package angry1980.audio.dao;

import angry1980.audio.model.Track;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public class TrackDAOFakeImpl implements TrackDAO{

    @Override
    public Optional<Track> get(long id) {
        return Optional.empty();
    }

    @Override
    public Collection<Track> findByCluster(long cluster) {
        return Collections.emptyList();
    }

    @Override
    public Optional<Collection<Track>> tryToGetAll() {
        return Optional.empty();
    }
}
