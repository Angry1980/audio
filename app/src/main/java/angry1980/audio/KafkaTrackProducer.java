package angry1980.audio;

import angry1980.audio.config.KafkaConfig;
import angry1980.audio.config.LocalConfig;
import angry1980.audio.model.Track;
import angry1980.audio.service.TrackSimilarityService;
import com.google.common.collect.ImmutableMap;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import rx.Subscriber;

import java.util.concurrent.CountDownLatch;

@Configuration
@Import(AppConfig.class)
public class KafkaTrackProducer {

    private static Logger LOG = LoggerFactory.getLogger(KafkaTrackProducer.class);

    @Autowired
    private TrackSimilarityService trackSimilarityService;
    @Autowired
    private Producer<Long, Track> kafkaProducer;
    @Autowired
    private Environment env;

    public static void main(String[] args){
        SpringApplication sa = new SpringApplication(KafkaTrackProducer.class);
        //todo: as program argument
        sa.setDefaultProperties(ImmutableMap.of(
                LocalConfig.INPUT_DIRECTORY_PROPERTY_NAME, "c:\\music",
                KafkaConfig.TRACKS_TOPIC_PROPERTY_NAME, "tracks"
        ));
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
                .subscribe(new SubscriberImpl(env.getProperty(KafkaConfig.TRACKS_TOPIC_PROPERTY_NAME), latch));
    }

    public class SubscriberImpl extends Subscriber<Track> {

        private String topic;
        private CountDownLatch latch;

        public SubscriberImpl(String topic, CountDownLatch latch) {
            this.latch = latch;
            this.topic = topic;
        }

        @Override
        public void onError(Throwable throwable) {
            LOG.error("Error while track sending to kafka", throwable);
        }

        @Override
        public void onNext(Track track) {
            try{
                kafkaProducer.send(new ProducerRecord<>(topic, track.getId(), track));
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
