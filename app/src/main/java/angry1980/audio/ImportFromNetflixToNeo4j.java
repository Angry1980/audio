package angry1980.audio;

import angry1980.audio.model.FingerprintType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(ImportFromNetflixToNeo4jConfig.class)
public class ImportFromNetflixToNeo4j {

    private static Logger LOG = LoggerFactory.getLogger(ImportFromNetflixToNeo4j.class);

    @Autowired
    private DataImporter dataImporter;
    @Autowired
    private DataImporter.TrackDataEnvironment sourceEnvironment;

    public static void main(String[] args){
        SpringApplication sa = new SpringApplication(ImportFromNetflixToNeo4j.class);
        sa.setAdditionalProfiles(
                //todo: as input arg
                FingerprintType.CHROMAPRINT.name(),
                FingerprintType.PEAKS.name(),
                FingerprintType.LASTFM.name(),
                "IMPORT",
                "NEO4J"
        );
        ConfigurableApplicationContext context = sa.run(args);
        context.registerShutdownHook();
        context.getBean(ImportFromNetflixToNeo4j.class).importData();
    }

    public ImportFromNetflixToNeo4j importData(){
        dataImporter.importTo(sourceEnvironment);
        LOG.info("Data was imported");
        return this;
    }


}
