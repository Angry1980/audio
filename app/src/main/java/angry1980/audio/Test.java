package angry1980.audio;

import angry1980.audio.dao.*;
import angry1980.audio.model.FingerprintType;
import angry1980.audio.model.Track;
import angry1980.audio.model.TrackSimilarity;
import angry1980.audio.service.TrackService;
import angry1980.audio.service.TrackSimilarityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import rx.Subscriber;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootApplication
@ComponentScan(value = {"angry1980.audio.config"})
public class Test extends Subscriber<TrackSimilarityService.TrackSimilarities>{

    private static Logger LOG = LoggerFactory.getLogger(Test.class);
//todo:
//similarity type - comparing, minhash, errorrates
// parameters for different implementations to props file
//wavelet
//autotests
//process, process waiter refactoring
//maven release

    @Autowired
    private TrackDAO trackDAO;
    @Autowired
    private TrackService trackService;
    @Autowired
    private TrackSimilarityService trackSimilarityService;
    @Autowired
    private TrackSimilarityDAO trackSimilarityDAO;

    public static void main(String[] args){
        SpringApplication sa = new SpringApplication(Test.class);
        sa.setAdditionalProfiles(
                FingerprintType.CHROMAPRINT.name(),
                FingerprintType.PEAKS.name()//,
                //FingerprintType.LASTFM.name()
        );
        ConfigurableApplicationContext context = sa.run(args);
        //todo: add shutdown hook
        Test test = context.getBean(Test.class);
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
    public void onNext(TrackSimilarityService.TrackSimilarities result) {
        LOG.info("Similarity calculation for {} is finished", result.getTrack());
    }

    @Override
    public void onCompleted() {
        trackDAO.getAllOrEmpty().stream()
                .peek(track -> LOG.info("{} looks like", track))
                .map(track -> trackSimilarityDAO.findByTrackId(track.getId()))
                .map(list -> list.orElseGet(() -> Collections.<TrackSimilarity>emptyList()))
                .flatMap(list -> list.stream()
                                    .collect(Collectors.groupingBy(ts -> ts.getTrack2()))
                                    .entrySet().stream()
                )
                .map(Object::toString)
                .forEach(LOG::info)
        ;

    }
}
