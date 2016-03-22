package angry1980.audio.config;

import angry1980.audio.Adapter;
import angry1980.audio.FileTracksProvider;
import angry1980.audio.LocalAdapter;
import angry1980.audio.dao.TrackDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class LocalConfig {

    public static final String INPUT_DIRECTORY_PROPERTY_NAME = "music.input.folder";

    @Autowired
    private Environment env;
    @Autowired
    private TrackDAO trackDAO;

    @Bean
    public Adapter adapter(){
        return new LocalAdapter();
    }

    @Bean
    @ConditionalOnProperty(INPUT_DIRECTORY_PROPERTY_NAME)
    public FileTracksProvider tracksProvider(){
        return new FileTracksProvider(env.getProperty(INPUT_DIRECTORY_PROPERTY_NAME), trackDAO);
    }


}
