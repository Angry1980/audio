package angry1980.audio.dao;

import angry1980.audio.model.Track;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class TrackDAOFileImpl implements TrackDAO{

    private List<Path> files;

    public TrackDAOFileImpl(List<Path> files) {
        this.files = Objects.requireNonNull(files);
    }

    @Override
    public Optional<Track> get(long id) {
        if(id > files.size() - 1){
            return Optional.empty();
        }
        return Optional.of(files.get((int) id))
                        .map(file -> new Track(id, file.toString()));
    }
}
