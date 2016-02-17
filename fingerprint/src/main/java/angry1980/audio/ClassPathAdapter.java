package angry1980.audio;

import angry1980.audio.model.Track;

import java.io.File;

public class ClassPathAdapter implements Adapter{

    @Override
    public File tryToGetContent(Track track) throws Exception {
        return new File(this.getClass().getResource(track.getPath()).toURI());
    }

}
