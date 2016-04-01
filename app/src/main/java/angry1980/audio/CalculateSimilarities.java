package angry1980.audio;

import angry1980.audio.config.KafkaProducerConsumerConfig;
import angry1980.audio.config.LocalConfig;
import angry1980.audio.config.NetflixConfig;
import angry1980.audio.kafka.ImmutableConsumerProperties;
import angry1980.audio.kafka.TrackDeserializer;
import angry1980.audio.model.ComparingType;
import angry1980.audio.model.TrackSimilarity;
import angry1980.audio.service.TrackSimilarityService;
import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import rx.Subscriber;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
@Import(value = {AppConfig.class, CalculateSimilaritiesConfig.class})
public class CalculateSimilarities {

    private static Logger LOG = LoggerFactory.getLogger(CalculateSimilarities.class);

    @Autowired
    private TrackSimilarityService trackSimilarityService;

    public static void main(String[] args){
        SpringApplication sa = new SpringApplication(CalculateSimilarities.class);
        //todo: as program arguments
        sa.setDefaultProperties(ImmutableMap.of(
                //local source
                LocalConfig.INPUT_DIRECTORY_PROPERTY_NAME, "c:\\music",
                NetflixConfig.SIMILARITY_FILE_PROPERTY_NAME, "c:\\work\\ts.data"
                // as kafka consumer
/*
                KafkaProducerConsumerConfig.SERVERS_PROPERTY_NAME, "localhost:9092",
                KafkaProducerConsumerConfig.CONSUMER_ENABLED_PROPERTY_NAME, "true",
                KafkaProducerConsumerConfig.CONSUMER_PROPERTIES,
                    ImmutableConsumerProperties.builder()
                            .valueDeserializer(TrackDeserializer.class)
                            .groupName("chromaprintSimilaritiesCalculator")
                            .topicName("tracks")
                            .build(),
                KafkaProducerConsumerConfig.PRODUCER_TOPIC_PROPERTY_NAME, "similarities"
                */
        ));
        sa.setRegisterShutdownHook(true);
        sa.setLogStartupInfo(false);
        ConfigurableApplicationContext context = sa.run(args);
        LOG.info("Starting application");
        CountDownLatch latch = new CountDownLatch(1);
        context.getBean(CalculateSimilarities.class).run(latch);
        try {
            latch.await();
        } catch (InterruptedException e) {
            LOG.error("Error hile calculating similarities", e);
        }
        context.close();
    }

    public void run(CountDownLatch latch){
        trackSimilarityService.getTracksToCalculateSimilarity()
                .doOnNext(track -> LOG.info("Similarity calculation for {}", track))
                .flatMap(track -> trackSimilarityService.findOrCalculateSimilarities(track,
                                                ComparingType.CHROMAPRINT,
                                                //ComparingType.LASTFM,
                                                ComparingType.CHROMAPRINT_ER,
                                                //ComparingType.LASTFM_ER,
                                                ComparingType.PEAKS)
                )//.subscribeOn(Schedulers.from(executor))
                .subscribe(new SubscriberImpl(latch));
    }

    public class SubscriberImpl extends Subscriber<TrackSimilarity>{

        private final AtomicInteger counter = new AtomicInteger();
        private final CountDownLatch latch;
        private Long2ObjectMap<Long2ObjectMap<Collection<TrackSimilarity>>> similarities = new Long2ObjectOpenHashMap<>();

        public SubscriberImpl(CountDownLatch latch){
            this.latch = latch;
        }

        @Override
        public void onError(Throwable throwable) {
            LOG.error("Error while track similarity calculation", throwable);
            latch.countDown();
        }

        @Override
        public void onNext(TrackSimilarity ts) {
            similarities.computeIfAbsent(ts.getTrack1(), t1 -> new Long2ObjectOpenHashMap<>())
                    .computeIfAbsent(ts.getTrack2(), t2 -> new HashSet<>()).add(ts);
            LOG.info("Result {} of similarity calculation {} was added", counter.getAndIncrement(), ts);
        }

        @Override
        public void onCompleted() {
            print();
            latch.countDown();
        }

        void print(){
            LOG.info("There were calculated {} similarities", counter.get());
            similarities.long2ObjectEntrySet().stream()
                    .peek(entry -> LOG.info("{} looks like", entry.getLongKey()))
                    .forEach(entry -> entry.getValue().entrySet().stream()
                            .map(Object::toString)
                            .forEach(LOG::info)
                    );
        }
    }
}
