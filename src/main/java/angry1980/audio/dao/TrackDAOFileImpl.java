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
        this(files, Collections.emptyList());
    }

    public TrackDAOFileImpl(List<Path> files, List<String> clusters) {
        Objects.requireNonNull(files);
        this.tracks = LongStream.range(0, files.size())
                        .mapToObj(id -> new Track(id, files.get((int) id).toString()))
                        .map(t -> t.setCluster(getCluster(clusters, t.getPath())))
                        .collect(Collectors.toMap(t -> t.getId(), Function.identity()))
        ;
    }

    private long getCluster(List<String> clusters, String path){
        for(int i = 0; i < clusters.size(); i++){
            if(path.startsWith(clusters.get(i))){
                return i;
            }
        }
        return 0;
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
