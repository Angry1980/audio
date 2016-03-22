package angry1980.audio.config;

import angry1980.audio.service.TrackSimilarityStatsService;
import angry1980.audio.service.TrackSimilarityStatsServiceNeo4jImpl;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.File;

@Configuration
@ConditionalOnProperty(Neo4jConfig.DATA_PATH_PROPERTY_NAME)
public class Neo4jConfig implements InitializingBean{

    public static final String DATA_PATH_PROPERTY_NAME = "music.similarity.db.path";

    @Autowired
    private Environment env;

    @Bean
    public GraphDatabaseService graphDatabaseService() {
        return new GraphDatabaseFactory()
                    .newEmbeddedDatabaseBuilder(new File(env.getProperty(DATA_PATH_PROPERTY_NAME)))
                    //.setConfig(GraphDatabaseSettings.pagecache_memory, "512M" )
                    .newGraphDatabase()
        ;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> graphDatabaseService().shutdown()));
    }

    @Bean
    public TrackSimilarityStatsService trackSimilarityStatsService(){
        return new TrackSimilarityStatsServiceNeo4jImpl(graphDatabaseService());
    }
}
