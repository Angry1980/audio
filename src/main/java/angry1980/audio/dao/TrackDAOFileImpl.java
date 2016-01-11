package angry1980.audio.dao;

import angry1980.audio.model.Track;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class TrackDAOFileImpl implements TrackDAO{

    private Map<Long, Track> tracks;

    public TrackDAOFileImpl(List<Path> files) {
        Objects.requireNonNull(files);
        this.tracks = LongStream.range(0, files.size())
                        .mapToObj(id -> new Track(id, files.get((int) id).toString()))
                        .collect(Collectors.toMap(t -> t.getId(), Function.identity()))
        ;
    }

    @Override
    public Optional<Track> get(long id) {
        return Optional.ofNullable(tracks.get(id));
    }

    @Override
    public Optional<Collection<Track>> tryToGetAll() {
        return Optional.of(tracks.values());

    }
}
