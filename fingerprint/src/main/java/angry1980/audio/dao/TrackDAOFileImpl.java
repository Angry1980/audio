package angry1980.audio.dao;

import angry1980.audio.model.Track;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class TrackDAOFileImpl implements TrackDAO{

    private Map<Long, Track> tracks;

    public TrackDAOFileImpl(List<Path> files){
        Objects.requireNonNull(files);
        this.tracks = new HashMap<>();
        LongStream.range(0, files.size())
                .mapToObj(id -> new Track(id, files.get((int) id).toString()))
                .forEach(this::create)
        ;
    }

    public TrackDAOFileImpl(Map<Long, Map<Long, Path>> files) {
        Objects.requireNonNull(files);
        this.tracks = new HashMap<>();
        files.entrySet().stream()
                .flatMap(entry -> entry.getValue().entrySet().stream()
                                    .map(file -> new Track(file.getKey(), file.getValue().toString()))
                                    .map(t -> t.setCluster(entry.getKey()))
                ).forEach(this::create)
        ;
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
