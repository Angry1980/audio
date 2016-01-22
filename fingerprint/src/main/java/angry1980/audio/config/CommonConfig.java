package angry1980.audio.config;

import angry1980.audio.service.TrackSimilarityService;
import angry1980.audio.service.TrackSimilarityServiceImpl;
import angry1980.audio.similarity.FindSimilarTracks;
import angry1980.audio.similarity.FindSimilarTracksFakeImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class CommonConfig {

    @Autowired
    private List<FindSimilarTracks> findSimilarTracksList;

    @Bean
    public FindSimilarTracks fakeFindSimilarTracks(){
        return new FindSimilarTracksFakeImpl();
    }

    @Bean
    public TrackSimilarityService trackSimilarityService(){
        return new TrackSimilarityServiceImpl(findSimilarTracksList);
    }
}
