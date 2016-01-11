package angry1980.audio;

import java.io.InputStream;
import java.util.Optional;

public interface Adapter {

    Optional<InputStream> getContent(String path);

}
