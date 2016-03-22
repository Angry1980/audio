package angry1980.audio;

import angry1980.audio.model.Track;
import angry1980.audio.service.TrackSimilarityService;
import com.google.common.collect.ImmutableMap;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import rx.Subscriber;

import java.util.concurrent.CountDownLatch;

@Configuration
@EnableAutoConfiguration
@ComponentScan(value = {"angry1980.audio.config"})
public class KafkaTrackProducer {

    private static Logger LOG = LoggerFactory.getLogger(KafkaTrackProducer.class);

    @Autowired
    private TrackSimilarityService trackSimilarityService;
    @Autowired
    private Producer<Long, String> kafkaProducer;

    public static void main(String[] args){
        SpringApplication sa = new SpringApplication(KafkaTrackProducer.class);
        sa.setAdditionalProfiles(
                "NETFLIX",
                "CALCULATE",
                "KAFKA"
        );
        //sa.setDefaultProperties(ImmutableMap.of("music.similarity.data.save", false));
        ConfigurableApplicationContext context = sa.run(args);
        context.registerShutdownHook();
        CountDownLatch latch = new CountDownLatch(1);
        LOG.info("Starting track producer application");
        context.getBean(KafkaTrackProducer.class).run(latch);
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
                kafkaProducer.send(new ProducerRecord<>("tracks", track.getId(), Long.toString(track.getId())));
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
