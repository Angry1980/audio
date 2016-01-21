package angry1980.audio.config;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
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
}
