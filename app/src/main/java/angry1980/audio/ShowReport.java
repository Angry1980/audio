package angry1980.audio;

import angry1980.audio.model.FingerprintType;
import angry1980.audio.service.TrackSimilarityStatsService;
import angry1980.utils.SpringMapWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import rx.Subscriber;

@SpringBootApplication
@Import(Neo4jDAOConfig.class)
public class ShowReport {

    private static Logger LOG = LoggerFactory.getLogger(ShowReport.class);

    @Autowired
    private TrackSimilarityStatsService trackSimilarityStatsService;

    public static void main(String[] args){
        SpringApplication sa = new SpringApplication(ShowReport.class);
        sa.setAdditionalProfiles(
                FingerprintType.CHROMAPRINT.name(),
                FingerprintType.PEAKS.name(),
                FingerprintType.LASTFM.name(),
                "NEO4J"
        );
        ConfigurableApplicationContext context = sa.run(args);
        context.registerShutdownHook();
        context.getBean(ShowReport.class).print();
    }

    public void print(){
        Subscriber printer = new Subscriber() {
            @Override
            public void onCompleted() {}

            @Override
            public void onError(Throwable e) {
                LOG.error("", e);
            }

            @Override
            public void onNext(Object o) {
                LOG.info(o.toString());
            }
        };
        trackSimilarityStatsService.compareFingerprintTypes().subscribe(printer);
        /*
        trackSimilarityStatsService.generateClusters().entrySet().stream()
                .peek(entry -> LOG.info("Cluster {} contains", entry.getKey()))
                .forEach(entry -> entry.getValue().stream()
                                    .map(Object::toString)
                                    .forEach(LOG::info)
                );
                */
    }


}
