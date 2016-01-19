package angry1980.audio.dao;

import angry1980.audio.dsl.TrackDSL;
import angry1980.audio.model.Track;


import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

public class TrackDAODslImpl implements TrackDAO {

    private TrackDSL trackDSL;

    public TrackDAODslImpl(TrackDSL trackDSL) {
        this.trackDSL = Objects.requireNonNull(trackDSL);
    }

    @Override
    public Track tryToGet(long id) {
        return null;
    }

    @Override
    public Collection<Track> tryToGetAll() {
        return null;
    }

    @Override
    public Track tryToCreate(Track entity) {
        trackDSL.track(entity.getId()).is(entity.getCluster());
        return entity;
    }

    @Override
    public Collection<Track> findByCluster(long cluster) {
        return null;
    }
}
