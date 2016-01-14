package angry1980.audio;

import angry1980.audio.model.Track;

import java.io.File;
import java.util.Optional;

public interface Adapter {

    default Optional<File> getContent(Track track){
        try {
            return Optional.ofNullable(tryToGetContent(track));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();

    }

    //to support java versions that do not support Optional
    File tryToGetContent(Track track) throws Exception;

    default void clean(){

    }
}
