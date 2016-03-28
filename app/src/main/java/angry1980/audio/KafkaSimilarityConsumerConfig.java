package angry1980.audio;

import angry1980.audio.dao.TrackSimilarityDAO;
import angry1980.audio.dao.TrackSimilarityDAOCassandraImpl;
import com.datastax.driver.core.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaSimilarityConsumerConfig {

    @Autowired
    private Session session;

    @Bean
    public TrackSimilarityDAO trackSimilarityDAO(){
        return new TrackSimilarityDAOCassandraImpl(session, "audio");
    }
}
