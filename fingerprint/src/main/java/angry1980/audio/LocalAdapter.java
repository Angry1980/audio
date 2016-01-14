package angry1980.audio;

import angry1980.audio.model.Track;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class LocalAdapter implements Adapter{

    @Override
    public File tryToGetContent(Track track) throws IOException {
        return Paths.get(track.getPath()).toFile();
    }
}
