package angry1980.audio;

import angry1980.audio.model.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Optional;

public interface Adapter {

    Logger LOG = LoggerFactory.getLogger(Adapter.class);

    default Optional<File> getContent(Track track){
        try {
            return Optional.ofNullable(tryToGetContent(track));
        } catch (Exception e) {
            LOG.error("Error while trying to get content of audio file", e);
        }
        return Optional.empty();

    }

    //to support java versions that do not support Optional
    File tryToGetContent(Track track) throws Exception;

    default void clean(){

    }
}
