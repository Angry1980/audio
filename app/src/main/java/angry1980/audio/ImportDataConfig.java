package angry1980.audio;

import angry1980.audio.dao.*;
import angry1980.audio.dsl.InMemoryTrackDSL;
import angry1980.audio.dsl.TrackDSL;
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
                new TrackDAODslImpl(trackDSLToImport(), new TrackDAOInMemoryImpl()),
                new TrackSimilarityDAODslImpl(trackDSLToImport())
        );
    }

    @Bean
    public TrackDSL trackDSLToImport(){
        return new InMemoryTrackDSL();
    }

}
