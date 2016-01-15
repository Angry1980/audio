package angry1980.audio.dao;

import angry1980.audio.model.Track;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public interface TrackDAO {

    default Optional<Track> get(long  id){
        return Optional.ofNullable(tryToGet(id));
    }

    Track tryToGet(long id);

    Collection<Track> findByCluster(long cluster);

    //todo: use paging
    default Optional<Collection<Track>> getAll(){
        return Optional.ofNullable(tryToGetAll());
    }

    //to support java versions less then 8
    Collection<Track> tryToGetAll();

    default Optional<Track> create(Track track){
        return Optional.of(tryToCreate(track));
    }

    Track tryToCreate(Track track);
}
