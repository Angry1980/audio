package angry1980.audio.config;

import angry1980.audio.dao.TrackDAO;
import angry1980.audio.dao.TrackSimilarityDAO;
import angry1980.audio.service.TrackSimilarityService;
import angry1980.audio.service.TrackSimilarityServiceImpl;
import angry1980.audio.similarity.ComplexFindSimilarTracks;
import angry1980.audio.similarity.FindSimilarTracks;
import angry1980.audio.similarity.TracksToCalculate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class CommonConfig {

    @Autowired
    private TrackDAO trackDAO;
    @Autowired
    private TrackSimilarityDAO trackSimilarityDAO;
    @Autowired
    private TracksToCalculate tracksToCalculate;

    @Bean
    public TrackSimilarityServiceImpl trackSimilarityService(){
        return new TrackSimilarityServiceImpl(
                trackDAO,
                trackSimilarityDAO,
                tracksToCalculate
        );
    }

}
