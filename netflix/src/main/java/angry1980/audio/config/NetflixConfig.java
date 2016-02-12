package angry1980.audio.config;

import angry1980.audio.dao.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.File;

@Configuration
@Profile("NETFLIX")
public class NetflixConfig {

    @Value("${music.similarity.data.file}")
    private String tsDataFile;

    @Bean
    public TrackSimilarityDAO trackSimilarityDAO(){
        return new TrackSimilarityDAONetflixImpl(netflixData());
    }

    @Bean
    public TrackDAO trackDAO(){
        return new TrackDAONetflixImpl(netflixData());
    }

    @Bean
    public NetflixData netflixData(){
        return new NetflixData();
    }


    @Bean(destroyMethod = "save")
    public NetflixDataProvider netflixDataProvider(){
        return new NetflixDataProvider(new File(tsDataFile), netflixData());
    }


}
