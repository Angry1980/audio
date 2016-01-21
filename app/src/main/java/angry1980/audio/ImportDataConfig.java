package angry1980.audio;

import angry1980.audio.dao.*;
import org.neo4j.graphdb.GraphDatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(value = {"angry1980.audio.config"})
public class ImportDataConfig {

    @Autowired
    private TrackDAO trackDAO;
    @Autowired
    private TrackSimilarityDAO trackSimilarityDAO;
    @Autowired
    private GraphDatabaseService graphDB;

    @Bean
    public DataImporter dataImporter(){
        return new DataImporter(
                new DataImporter.TrackDataEnvironment(
                        trackDAO,
                        trackSimilarityDAO
                )
        );
    }

    @Bean
    public DataImporter.TrackDataEnvironment sourceEnvironment(){
        return new DataImporter.TrackDataEnvironment(
                new TrackDAONeo4jImpl(graphDB),
                new TrackSimilarityDAONeo4jImpl(graphDB)
        );
    }

}
