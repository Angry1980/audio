package angry1980.audio.config;

import angry1980.audio.service.TrackSimilarityStatsService;
import angry1980.audio.service.TrackSimilarityStatsServiceNeo4jImpl;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.File;

@Configuration
@Profile("NEO4J")
public class Neo4jConfig implements InitializingBean{

    @Bean
    public GraphDatabaseService graphDatabaseService() {
        return new GraphDatabaseFactory()
                    .newEmbeddedDatabaseBuilder(new File("c://work/ts.graphdb"))
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
