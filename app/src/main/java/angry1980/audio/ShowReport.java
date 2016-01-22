package angry1980.audio;

import angry1980.audio.model.FingerprintType;
import angry1980.audio.service.TrackSimilarityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(ShowReportConfig.class)
public class ShowReport {

    private static Logger LOG = LoggerFactory.getLogger(ShowReport.class);

    @Autowired
    private DataImporter dataImporter;
    @Autowired
    private DataImporter.TrackDataEnvironment sourceEnvironment;
    @Autowired
    private TrackSimilarityService trackSimilarityService;

    public static void main(String[] args){
        SpringApplication sa = new SpringApplication(ShowReport.class);
        sa.setAdditionalProfiles(
                FingerprintType.CHROMAPRINT.name(),
                FingerprintType.PEAKS.name(),
                FingerprintType.LASTFM.name(),
                "IMPORT",
                "NEO4J"
        );
        ConfigurableApplicationContext context = sa.run(args);
        context.registerShutdownHook();
        context.getBean(ShowReport.class).importData().print();
    }

    public ShowReport importData(){
        dataImporter.importTo(sourceEnvironment);
        return this;
    }

    public void print(){
        trackSimilarityService.getReport()
                .subscribe(ts -> {
                    LOG.info("{} looks like", ts.getTrack());
                    ts.groupByTrack().entrySet().stream()
                        .map(Object::toString)
                        .forEach(LOG::info);
                });
    }

}
