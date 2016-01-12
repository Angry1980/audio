package angry1980.audio.dao;

import angry1980.audio.model.Track;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class TrackDAOFileImpl implements TrackDAO{

    private Map<Long, Track> tracks;

    public TrackDAOFileImpl(List<Path> files){
        Objects.requireNonNull(files);
        this.tracks = LongStream.range(0, files.size())
                .mapToObj(id -> new Track(id, files.get((int) id).toString()))
                .collect(Collectors.toMap(t -> t.getId(), Function.identity()))
        ;

    }

    public TrackDAOFileImpl(Map<Long, Map<Long, Path>> files) {
        Objects.requireNonNull(files);
        this.tracks = files.entrySet().stream()
                .flatMap(entry -> entry.getValue().entrySet().stream()
                                    .map(file -> new Track(file.getKey(), file.getValue().toString()))
                                    .map(t -> t.setCluster(entry.getKey()))
                ).collect(Collectors.toMap(t -> t.getId(), Function.identity()))
        ;
    }

    @Override
    public Optional<Track> get(long id) {
        return Optional.ofNullable(tracks.get(id));
    }

    @Override
    public Collection<Track> findByCluster(long cluster) {
        if(cluster == 0){
            return getAll();
        }
        return tracks.values().stream()
                .filter(track -> track.getCluster() == cluster)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Collection<Track>> tryToGetAll() {
        return Optional.of(tracks.values());

    }
}
