package angry1980.audio;

import angry1980.audio.config.KafkaProducerConsumerConfig;
import angry1980.audio.config.NetflixConfig;
import angry1980.audio.dao.NetflixDataProvider;
import angry1980.audio.dao.TrackDAO;
import angry1980.audio.dao.TrackSimilarityDAO;
import angry1980.audio.kafka.ImmutableConsumerProperties;
import angry1980.audio.kafka.StreamConsumer;
import angry1980.audio.kafka.TrackSimilarityDeserializer;
import angry1980.audio.model.ImmutableTrack;
import angry1980.audio.model.Track;
import angry1980.audio.model.TrackSimilarity;
import com.google.common.collect.ImmutableMap;
import org.apache.kafka.clients.consumer.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;



@Configuration
@Import(AppConfig.class)
public class KafkaSimilarityConsumer {

    private static Logger LOG = LoggerFactory.getLogger(KafkaSimilarityConsumer.class);

    @Autowired
    private Consumer kafkaConsumer;
    @Autowired
    private TrackDAO trackDAO;
    @Autowired
    private TrackSimilarityDAO similarityDAO;
    @Autowired
    private NetflixDataProvider dataProvider;

    public static void main(String[] args){
        SpringApplication sa = new SpringApplication(KafkaSimilarityConsumer.class);
        //todo: as program arguments
        sa.setDefaultProperties(ImmutableMap.of(
                NetflixConfig.SIMILARITY_FILE_PROPERTY_NAME, "c:\\work\\ts.data",
                KafkaProducerConsumerConfig.SERVERS_PROPERTY_NAME, "localhost:9092",
                KafkaProducerConsumerConfig.CONSUMER_ENABLED_PROPERTY_NAME, "true",
                KafkaProducerConsumerConfig.CONSUMER_PROPERTIES,
                ImmutableConsumerProperties.builder()
                        .valueDeserializer(TrackSimilarityDeserializer.class)
                        .topicName("similarities")
                        .groupName("similarityStorage")
                        .build()
        ));
        ConfigurableApplicationContext context = sa.run(args);
        context.registerShutdownHook();
        LOG.info("Starting application");
        context.getBean(KafkaSimilarityConsumer.class).run();
        context.close();
    }

    public void run(){
        Track from = ImmutableTrack.builder().id(0).cluster(0).path("").build();
        new StreamConsumer<Long, TrackSimilarity>(kafkaConsumer).get()
                .doOnNext(ts -> {
                    //todo: hot fix
                    trackDAO.create(ImmutableTrack.builder().from(from).id(ts.getTrack1()).build());
                    trackDAO.create(ImmutableTrack.builder().from(from).id(ts.getTrack2()).build());
                    similarityDAO.create(ts);
                })
                .forEach(ts -> {
                    LOG.info(ts.toString());
                    dataProvider.save();
                });
    }

}
