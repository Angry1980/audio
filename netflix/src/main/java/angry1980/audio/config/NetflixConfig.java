package angry1980.audio.config;

import angry1980.audio.dao.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.File;

@Configuration
public class NetflixConfig {

    public static final String SIMILARITY_FILE_PROPERTY_NAME = "music.similarity.data.file";

    @Autowired
    private Environment env;

    @Bean
    @ConditionalOnMissingBean(TrackSimilarityDAO.class)
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
    @ConditionalOnProperty(SIMILARITY_FILE_PROPERTY_NAME)
    public NetflixDataProvider netflixDataProvider(){
        return new NetflixDataProvider(new File(env.getProperty(SIMILARITY_FILE_PROPERTY_NAME)), netflixData());
    }

}
