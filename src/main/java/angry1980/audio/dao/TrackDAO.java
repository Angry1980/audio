package angry1980.audio.dao;

import angry1980.audio.model.Track;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public interface TrackDAO {

    Optional<Track> get(long  id);

    //todo: use paging
    default Collection<Track> getAll(){
        return tryToGetAll().orElseGet(() -> Collections.emptyList());
    }

    Optional<Collection<Track>> tryToGetAll();
}
