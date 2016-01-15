package angry1980.audio.dao;

import angry1980.audio.model.Track;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public class TrackDAOFakeImpl implements TrackDAO{

    @Override
    public Track tryToGet(long id) {
        return null;
    }

    @Override
    public Collection<Track> findByCluster(long cluster) {
        return Collections.emptyList();
    }

    @Override
    public Collection<Track> tryToGetAll() {
        return null;
    }

    @Override
    public Optional<Track> create(Track track) {
        return Optional.empty();
    }

    @Override
    public Track tryToCreate(Track track) {
        return null;
    }

}
