package angry1980.audio;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public interface Adapter {

    default Optional<InputStream> getContent(String path){
        try {
            return Optional.of(tryToGetContent(path));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();

    }

    //to support java versions that do not support Optional
    InputStream tryToGetContent(String path) throws Exception;

}
