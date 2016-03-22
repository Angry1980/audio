package angry1980.audio;

import angry1980.audio.config.Neo4jConfig;
import angry1980.audio.config.NetflixConfig;
import angry1980.audio.model.ComparingType;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(ImportFromNetflixToNeo4jConfig.class)
public class ImportFromNetflixToNeo4j {

    private static Logger LOG = LoggerFactory.getLogger(ImportFromNetflixToNeo4j.class);

    @Autowired
    private DataImporter dataImporter;
    @Autowired
    private DataImporter.TrackDataEnvironment sourceEnvironment;

    public static void main(String[] args){
        SpringApplication sa = new SpringApplication(ImportFromNetflixToNeo4j.class);
        //todo: as program argument
        sa.setDefaultProperties(ImmutableMap.of(
                NetflixConfig.SIMILARITY_FILE_PROPERTY_NAME, "c:\\work\\ts.data",
                Neo4jConfig.DATA_PATH_PROPERTY_NAME, "c:\\work\\ts.graphdb"
        ));
        ConfigurableApplicationContext context = sa.run(args);
        context.registerShutdownHook();
        context.getBean(ImportFromNetflixToNeo4j.class)
                .importData(ComparingType.CHROMAPRINT)
                .importData(ComparingType.CHROMAPRINT_ER)
                //.importData(ComparingType.LASTFM)
                //.importData(ComparingType.LASTFM_ER)
                .importData(ComparingType.PEAKS)
        ;
    }

    public ImportFromNetflixToNeo4j importData(ComparingType type){
        return importData(type, type);
    }

    public ImportFromNetflixToNeo4j importData(ComparingType type, ComparingType goal){
        dataImporter.importTo(sourceEnvironment, type, goal);
        LOG.info("Similarities for {} was imported", type);
        return this;
    }

}
