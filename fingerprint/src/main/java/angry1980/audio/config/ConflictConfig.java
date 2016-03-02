package angry1980.audio.config;

import angry1980.audio.service.TrackSimilarityServiceImpl;
import angry1980.audio.similarity.ComplexFindSimilarTracks;
import angry1980.audio.similarity.FindSimilarTracks;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.List;

@Configuration
@Order(Ordered.LOWEST_PRECEDENCE)
public class ConflictConfig implements InitializingBean {

    @Autowired
    private List<FindSimilarTracks> findSimilarTracksList;
    @Autowired
    private TrackSimilarityServiceImpl trackSimilarityService;

    @Override
    public void afterPropertiesSet() throws Exception {
        trackSimilarityService.setFindSimilarTracks(new ComplexFindSimilarTracks(findSimilarTracksList));
    }
}
