package angry1980.audio.dao;

import angry1980.audio.model.Track;

import java.util.*;
import java.util.stream.Collectors;

public class TrackDAOInMemoryImpl implements TrackDAO{

    private Map<Long, Track> tracks;

    public TrackDAOInMemoryImpl(){
        this.tracks = new HashMap<>();
    }

    @Override
    public Track tryToGet(long id) {
        return tracks.get(id);
    }

    @Override
    public Collection<Track> findByCluster(long cluster) {
        if(cluster == 0){
            return tryToGetAll();
        }
        return tracks.values().stream()
                .filter(track -> track.getCluster() == cluster)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Track> tryToGetAll() {
        return tracks.values();
    }

    @Override
    public Track tryToCreate(Track track) {
        tracks.put(track.getId(), track);
        return track;
    }
}
