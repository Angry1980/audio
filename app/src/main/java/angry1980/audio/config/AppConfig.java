package angry1980.audio.config;

import angry1980.audio.Adapter;
import angry1980.audio.LocalAdapter;
import angry1980.audio.FileTracksProvider;
import angry1980.audio.dao.*;
import angry1980.audio.dsl.NetflixDataProvider;
import angry1980.audio.dsl.NetflixTrackDSL;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.io.File;

@Configuration
@PropertySource({"classpath:local.properties"})
public class AppConfig {

    @Value("${music.input.folder}")
    private String inputFolder;
    @Value("${music.similarity.data.file}")
    private String tsDataFile;

    @Bean
    public TrackSimilarityDAO trackSimilarityDAO(){
        //return new TrackSimilarityDAOInMemoryImpl();
        return new TrackSimilarityDAODslImpl(netflixTrackDSL());
    }

    @Bean
    public NetflixTrackDSL netflixTrackDSL(){
        return new NetflixTrackDSL();
    }

    @Bean
    public Adapter adapter(){
        return new LocalAdapter();
    }

    @Bean
    public TrackDAO trackDAO(){
        return new TrackDAODslImpl(netflixTrackDSL(), new TrackDAOInMemoryImpl());
    }

    @Bean
    public FileTracksProvider tracksProvider(){
        return new FileTracksProvider(inputFolder, trackDAO());
    }

    @Bean(destroyMethod = "save")
    public NetflixDataProvider netflixDataProvider(){
        return new NetflixDataProvider(new File(tsDataFile), netflixTrackDSL());
    }
}
