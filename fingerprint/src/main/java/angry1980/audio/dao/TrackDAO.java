package angry1980.audio.dao;

import angry1980.audio.model.Track;

import java.util.Collection;

public interface TrackDAO extends DAO<Track>{

    Collection<Track> findByCluster(long cluster);

}
