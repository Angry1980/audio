package angry1980.audio.kafka;

import angry1980.audio.model.Track;
import angry1980.audio.service.TrackSimilarityService;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import rx.Subscriber;

import java.util.concurrent.CountDownLatch;

@SpringBootApplication
@Import(AppConfig.class)
public class TrackProducer {

    private static Logger LOG = LoggerFactory.getLogger(TrackProducer.class);

    @Autowired
    private TrackSimilarityService trackSimilarityService;
    @Autowired
    private Producer<Long, String> kafkaProducer;

    public static void main(String[] args){
        SpringApplication sa = new SpringApplication(TrackProducer.class);
        sa.setAdditionalProfiles(
                "NETFLIX"
        );
        ConfigurableApplicationContext context = sa.run(args);
        context.registerShutdownHook();
        CountDownLatch latch = new CountDownLatch(1);
        LOG.info("Starting track producer application");
        context.getBean(TrackProducer.class).run(latch);
        try {
            latch.await();
        } catch (InterruptedException e) {
            LOG.error("Error while application working", e);
        }
        context.close();
    }

    public void run(CountDownLatch latch){
        trackSimilarityService.getTracksToCalculateSimilarity()
                .doOnNext(track -> LOG.info("{} is ready to send to kafka", track))
                .subscribe(new SubscriberImpl(latch));
    }

    public class SubscriberImpl extends Subscriber<Track> {

        private CountDownLatch latch;

        public SubscriberImpl(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onError(Throwable throwable) {
            LOG.error("Error while track sending to kafka", throwable);
        }

        @Override
        public void onNext(Track track) {
            try{
                kafkaProducer.send(new ProducerRecord<>("uploaded-tracks", track.getId(), Long.toString(track.getId())));
                LOG.info("Sending {} to kafka is finished", track.getId());
            } catch (Exception e){
                LOG.error(e.getMessage());
            }
        }

        @Override
        public void onCompleted() {
            latch.countDown();
        }

    }

}
