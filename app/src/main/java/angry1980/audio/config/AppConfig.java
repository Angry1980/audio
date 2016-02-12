package angry1980.audio.config;

import angry1980.audio.Adapter;
import angry1980.audio.LocalAdapter;
import angry1980.audio.FileTracksProvider;
import angry1980.audio.dao.*;
import angry1980.audio.similarity.TracksToCalculateImpl;
import angry1980.audio.similarity.TracksToCalculate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
@PropertySource({"classpath:local.properties"})
public class AppConfig {

    @Value("${music.input.folder}")
    private String inputFolder;
    @Autowired
    private TrackDAO trackDAO;

    @Bean
    public ScheduledExecutorService executor(){
        return Executors.newScheduledThreadPool(3);
    }

    @Bean
    public Adapter adapter(){
        return new LocalAdapter();
    }

    @Bean
    @Profile("CALCULATE")
    public FileTracksProvider tracksProvider(){
        return new FileTracksProvider(inputFolder, trackDAO);
    }

    @Bean
    public TracksToCalculate tracksToCalculate(){
        return new TracksToCalculateImpl(trackDAO);
    }
}
