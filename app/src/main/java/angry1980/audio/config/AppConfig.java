package angry1980.audio.config;

import angry1980.audio.Adapter;
import angry1980.audio.LocalAdapter;
import angry1980.audio.FileTracksProvider;
import angry1980.audio.dao.*;
import angry1980.audio.dao.NetflixDataProvider;
import angry1980.audio.similarity.TracksToCalculateImpl;
import angry1980.audio.similarity.TracksToCalculate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
@PropertySource({"classpath:local.properties"})
public class AppConfig {

    @Value("${music.input.folder}")
    private String inputFolder;
    @Value("${music.similarity.data.file}")
    private String tsDataFile;

    @Bean
    public ScheduledExecutorService executor(){
        return Executors.newScheduledThreadPool(3);
    }

    @Bean
    public TrackSimilarityDAO trackSimilarityDAO(){
        return new TrackSimilarityDAONetflixImpl(netflixData());
    }

    @Bean
    public NetflixData netflixData(){
        return new NetflixData();
    }

    @Bean
    public Adapter adapter(){
        return new LocalAdapter();
    }

    @Bean
    public TrackDAO trackDAO(){
        return new TrackDAONetflixImpl(netflixData());
    }

    @Bean
    @Profile("!IMPORT")
    public FileTracksProvider tracksProvider(){
        return new FileTracksProvider(inputFolder, trackDAO());
    }

    @Bean(destroyMethod = "save")
    public NetflixDataProvider netflixDataProvider(){
        return new NetflixDataProvider(new File(tsDataFile), netflixData());
    }

    @Bean
    public TracksToCalculate tracksToCalculate(){
        return new TracksToCalculateImpl(trackDAO());
    }
}
