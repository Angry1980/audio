package angry1980.audio.neo4j;

import angry1980.audio.dao.*;
import org.neo4j.graphdb.GraphDatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Neo4jDAOConfig {

    @Autowired
    private GraphDatabaseService graphDB;

    @Bean
    public TrackDAO trackDAO(){
        return new TrackDAONeo4jImpl(graphDB);
    }

    @Bean
    public TrackSimilarityDAO trackSimilarityDAO(){
        return new TrackSimilarityDAONeo4jImpl(graphDB);
    }

}
