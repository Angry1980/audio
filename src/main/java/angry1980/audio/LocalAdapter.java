package angry1980.audio;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

public class LocalAdapter implements Adapter{

    @Override
    public Optional<InputStream> getContent(String path) {
        try {
            return Optional.of(Files.newInputStream(Paths.get(path), StandardOpenOption.READ));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}
