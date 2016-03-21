package angry1980.audio;

import angry1980.audio.model.ComparingType;
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
import rx.schedulers.Schedulers;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

@SpringBootApplication
@ComponentScan(value = {"angry1980.audio.config"})
public class CalculateSimilarities {

    private static Logger LOG = LoggerFactory.getLogger(CalculateSimilarities.class);

    @Autowired
    private Executor executor;
    @Autowired
    private TrackSimilarityService trackSimilarityService;

    public static void main(String[] args){
        SpringApplication sa = new SpringApplication(CalculateSimilarities.class);
        sa.setAdditionalProfiles(
                "NETFLIX",
                "CALCULATE",
                ComparingType.CHROMAPRINT.name(),
                ComparingType.PEAKS.name(),
                ComparingType.LASTFM.name()
        );
        ConfigurableApplicationContext context = sa.run(args);
        context.registerShutdownHook();
        CountDownLatch latch = new CountDownLatch(1);
        LOG.info("Starting application");
        context.getBean(CalculateSimilarities.class).run(latch);
        try {
            latch.await();
        } catch (InterruptedException e) {
            LOG.error("Error while application working", e);
        }
        context.close();
    }

    public void run(CountDownLatch latch){
        trackSimilarityService.getTracksToCalculateSimilarity()
                .doOnNext(track -> LOG.info("Similarity calculation for {}", track))
                //.flatMap(trackSimilarityService::findOrCalculateSimilarities)
                .flatMap(track -> trackSimilarityService.findOrCalculateSimilarities(track,
                                                ComparingType.CHROMAPRINT,
                                                //ComparingType.LASTFM,
                                                ComparingType.CHROMAPRINT_ER,
                                                //ComparingType.LASTFM_ER,
                                                ComparingType.PEAKS)
                ).subscribeOn(Schedulers.from(executor))
                .subscribe(new SubscriberImpl(latch));
    }

    public class SubscriberImpl extends Subscriber<TrackSimilarities>{

        private CountDownLatch latch;

        public SubscriberImpl(CountDownLatch latch) {
            this.latch = latch;
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
            latch.countDown();
        }

    }
}
