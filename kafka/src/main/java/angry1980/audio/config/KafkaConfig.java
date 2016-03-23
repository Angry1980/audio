package angry1980.audio.config;

import angry1980.audio.model.Track;
import angry1980.audio.similarity.TracksToCalculate;
import angry1980.audio.similarity.TracksToCalculateKafkaImpl;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Properties;

@Configuration
@ConditionalOnProperty(KafkaConfig.TRACKS_TOPIC_PROPERTY_NAME)
public class KafkaConfig {

    public static final String TRACKS_TOPIC_PROPERTY_NAME = "music.kafka.tracks.topic.name";
    public static final String TRACKS_SOURCE_PROPERTY_NAME = "music.kafka.tracks.source";

    @Autowired
    private Environment env;

    @Bean
    public Properties kafkaProducerProperties() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.LongSerializer");
        props.put("value.serializer", "angry1980.audio.kafka.TrackSerializer");
        return props;
    }

    @Bean(destroyMethod = "close")
    public Producer<Long, Track> kafkaProducer() {
        return new KafkaProducer<>(kafkaProducerProperties());
    }

    @Bean
    public Properties kafkaConsumerProperties() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "test");
        props.put("enable.auto.commit", "false");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.LongDeserializer");
        props.put("value.deserializer", "angry1980.audio.kafka.TrackSerializer");
        return props;
    }

    @Bean(destroyMethod = "stop")
    @ConditionalOnProperty(TRACKS_SOURCE_PROPERTY_NAME)
    public TracksToCalculate tracksToCalculate(){
        return new TracksToCalculateKafkaImpl(kafkaConsumerProperties(), env.getProperty(TRACKS_TOPIC_PROPERTY_NAME));
    }

}
