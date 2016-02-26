package angry1980.audio;

import angry1980.audio.model.FingerprintType;
import angry1980.audio.service.TrackSimilarityStatsService;
import angry1980.audio.stats.Stats;
import angry1980.utils.SpringMapWrapper;
import com.google.common.collect.ImmutableMap;
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
        Subscriber<Stats> printer = new Subscriber<Stats>() {

            Stats best = null;

            @Override
            public void onCompleted() {
                LOG.info("Best choice {}", best);
            }

            @Override
            public void onError(Throwable e) {
                LOG.error("", e);
            }

            @Override
            public void onNext(Stats o) {
                if(o == null){
                    return;
                }
                LOG.info(o.toString());
                if(o.getFalsePositive() == 0){
                    //bit comparing
                    return;
                }
                if(best == null || Double.compare(best.getF1(), o.getF1()) < 0){
                    best = o;
                }
            }
        };
        trackSimilarityStatsService.compareFingerprintTypes(ImmutableMap.of(
                        FingerprintType.CHROMAPRINT, 10,
                        FingerprintType.CHROMAPRINT_ER, 10,
                        FingerprintType.LASTFM, 10,
                        FingerprintType.LASTFM_ER, 10,
                        FingerprintType.PEAKS, 10
                )
        ).subscribe(printer);
/*
        trackSimilarityStatsService.compareFingerprintTypes(ImmutableMap.of(
                FingerprintType.CHROMAPRINT, 665,
                FingerprintType.CHROMAPRINT_ER, 1500,
                FingerprintType.LASTFM, 466,
                FingerprintType.LASTFM_ER, 844,
                FingerprintType.PEAKS, 1102
                )
        ).subscribe(printer);
*/
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
