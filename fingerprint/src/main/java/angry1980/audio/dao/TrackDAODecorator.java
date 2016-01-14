package angry1980.audio.dao;

import angry1980.audio.model.Track;

import java.util.Collection;
import java.util.Objects;

public class TrackDAODecorator implements TrackDAO{

    private TrackDAO prototype;

    public TrackDAODecorator(TrackDAO prototype) {
        this.prototype = Objects.requireNonNull(prototype);
    }

    @Override
    public Track tryToGet(long id) {
        return prototype.tryToGet(id);
    }

    @Override
    public Collection<Track> findByCluster(long cluster) {
        return prototype.findByCluster(cluster);
    }

    @Override
    public Collection<Track> tryToGetAll() {
        return prototype.tryToGetAll();
    }

    @Override
    public Track tryToCreate(Track track) {
        return prototype.tryToCreate(track);
    }
}
