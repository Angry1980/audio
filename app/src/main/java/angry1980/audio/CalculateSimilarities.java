package angry1980.audio;

import angry1980.audio.config.KafkaProducerConsumerConfig;
import angry1980.audio.kafka.ImmutableConsumerProperties;
import angry1980.audio.kafka.TrackDeserializer;
import angry1980.audio.model.ComparingType;
import angry1980.audio.service.TrackSimilarityService;
import angry1980.audio.similarity.TrackSimilarities;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import rx.Subscriber;

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
                //LocalConfig.INPUT_DIRECTORY_PROPERTY_NAME, "c:\\music",
                //NetflixConfig.SIMILARITY_FILE_PROPERTY_NAME, "c:\\work\\ts.data"
                // as kafka consumer
                KafkaProducerConsumerConfig.SERVERS_PROPERTY_NAME, "localhost:9092",
                KafkaProducerConsumerConfig.CONSUMER_ENABLED_PROPERTY_NAME, "true",
                KafkaProducerConsumerConfig.CONSUMER_PROPERTIES,
                    ImmutableConsumerProperties.builder()
                            .valueDeserializer(TrackDeserializer.class)
                            .groupName("chromaprintSimilaritiesCalculator")
                            .topicName("tracks")
                            .build(),
                KafkaProducerConsumerConfig.PRODUCER_TOPIC_PROPERTY_NAME, "similarities"
        ));
        sa.setRegisterShutdownHook(true);
        sa.setLogStartupInfo(false);
        ConfigurableApplicationContext context = sa.run(args);
        LOG.info("Starting application");
        context.getBean(CalculateSimilarities.class).run();
        context.close();
    }

    public void run(){
        trackSimilarityService.getTracksToCalculateSimilarity()
                .doOnNext(track -> LOG.info("Similarity calculation for {}", track))
                .flatMap(track -> trackSimilarityService.findOrCalculateSimilarities(track,
                                                ComparingType.CHROMAPRINT,
                                                //ComparingType.LASTFM,
                                                ComparingType.CHROMAPRINT_ER,
                                                //ComparingType.LASTFM_ER,
                                                ComparingType.PEAKS)
                )//.subscribeOn(Schedulers.from(executor))
                .subscribe(new SubscriberImpl());
    }

    public class SubscriberImpl extends Subscriber<TrackSimilarities>{


        public SubscriberImpl(){
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
}
