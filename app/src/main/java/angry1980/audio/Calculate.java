package angry1980.audio;

import angry1980.audio.model.FingerprintType;
import angry1980.audio.service.TrackService;
import angry1980.audio.service.TrackSimilarityService;
import angry1980.audio.similarity.TrackSimilarities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import rx.Subscriber;

@SpringBootApplication
@ComponentScan(value = {"angry1980.audio.config"})
public class Calculate extends Subscriber<TrackSimilarities>{

    private static Logger LOG = LoggerFactory.getLogger(Calculate.class);
//todo:
//similarity type - comparing, minhash, errorrates
// parameters for different implementations to props file
//wavelet
//autotests
//process, process waiter refactoring
//maven release

    @Autowired
    private TrackService trackService;
    @Autowired
    private TrackSimilarityService trackSimilarityService;

    public static void main(String[] args){
        SpringApplication sa = new SpringApplication(Calculate.class);
        sa.setAdditionalProfiles(
                FingerprintType.CHROMAPRINT.name(),
                FingerprintType.PEAKS.name()//,
                //FingerprintType.LASTFM.name()
        );
        ConfigurableApplicationContext context = sa.run(args);
        //todo: add shutdown hook
        Calculate test = context.getBean(Calculate.class);
        test.run();
    }

    public void run(){
        //todo: try few threads
        trackService.getTracksToCalculateSimilarity()
                .doOnNext(track -> LOG.info("Similarity calculation for {}", track))
                .flatMap(trackSimilarityService::findOrCalculateSimilarities)
                .subscribe(this);
    }

    @Override
    public void onError(Throwable throwable) {
        LOG.error("Error while track similarity calculation", throwable);
    }

    @Override
    public void onNext(TrackSimilarities result) {
        LOG.info("Similarity calculation for {} is finished", result.getTrack());
    }

    @Override
    public void onCompleted() {
        trackSimilarityService.getReport().subscribe(ts -> {
            LOG.info("{} looks like", ts.getTrack());
            ts.groupByTrack().entrySet().stream()
                    .map(Object::toString)
                    .forEach(LOG::info);
        });
    }
}
