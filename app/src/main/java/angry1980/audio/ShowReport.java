package angry1980.audio;

import angry1980.audio.model.ComparingType;
import angry1980.audio.service.TrackSimilarityStatsService;
import angry1980.audio.stats.Stats;
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
                ComparingType.CHROMAPRINT.name(),
                ComparingType.PEAKS.name(),
                ComparingType.LASTFM.name(),
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
                        ComparingType.CHROMAPRINT, 10,
                        ComparingType.CHROMAPRINT_ER, 10,
                        ComparingType.LASTFM, 10,
                        ComparingType.LASTFM_ER, 10,
                        ComparingType.PEAKS, 10
                )
        ).subscribe(printer);
/*
        trackSimilarityStatsService.compareFingerprintTypes(ImmutableMap.of(
                        ComparingType.CHROMAPRINT, 50,
                        ComparingType.CHROMAPRINT_ER, 10,
                        ComparingType.LASTFM, 367,
                        ComparingType.LASTFM_ER, 10,
                        ComparingType.PEAKS, 20
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
