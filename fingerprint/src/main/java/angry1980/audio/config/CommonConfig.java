package angry1980.audio.config;

import angry1980.audio.dao.TrackDAO;
import angry1980.audio.dao.TrackSimilarityDAO;
import angry1980.audio.service.TrackSimilarityServiceImpl;
import angry1980.audio.similarity.CompositeFindSimilarTracks;
import angry1980.audio.similarity.FindSimilarTracks;
import angry1980.audio.similarity.TracksToCalculate;
import angry1980.audio.similarity.TracksToCalculateImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Optional;

@Configuration
public class CommonConfig {

    @Autowired
    private TrackDAO trackDAO;
    @Autowired
    private TrackSimilarityDAO trackSimilarityDAO;
    @Autowired
    private Optional<List<FindSimilarTracks>> findSimilarTracksList;

    @Bean
    public TrackSimilarityServiceImpl trackSimilarityService(){
        return new TrackSimilarityServiceImpl(
                new CompositeFindSimilarTracks(findSimilarTracksList),
                trackDAO,
                trackSimilarityDAO,
                tracksToCalculate()
        );
    }

    @Bean
    @ConditionalOnMissingBean(TracksToCalculate.class)
    public TracksToCalculate tracksToCalculate(){
        return new TracksToCalculateImpl(trackDAO);
    }

}
