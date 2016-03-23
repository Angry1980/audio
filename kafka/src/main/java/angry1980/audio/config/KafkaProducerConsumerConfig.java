package angry1980.audio.config;

import angry1980.audio.model.Track;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.Properties;

@Configuration
@ConditionalOnProperty(KafkaProducerConsumerConfig.SERVERS_PROPERTY_NAME)
public class KafkaProducerConsumerConfig {

    public static final String SERVERS_PROPERTY_NAME = "music.kafka.servers.name";
    public static final String PRODUCER_SERIALIZER_PROPERTY_NAME = "music.kafka.producer.serializer.name";
    public static final String PRODUCER_TOPIC_PROPERTY_NAME = "music.kafka.producer.topic.name";
    public static final String CONSUMER_SERIALIZER_PROPERTY_NAME = "music.kafka.consumer.serializer.name";
    public static final String CONSUMER_TOPIC_PROPERTY_NAME = "music.kafka.consumer.topic.name";

    @Autowired
    private Environment env;

    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(value = {
            PRODUCER_TOPIC_PROPERTY_NAME,
            PRODUCER_SERIALIZER_PROPERTY_NAME
    })
    public Producer kafkaProducer() {
        Properties props = kafkaProducerProperties();
        props.put("key.serializer", "org.apache.kafka.common.serialization.LongSerializer");
        props.put("value.serializer", env.getProperty(PRODUCER_SERIALIZER_PROPERTY_NAME));
        return new KafkaProducer<>(props);
    }

    @Bean
    @ConditionalOnProperty(value ={
            CONSUMER_TOPIC_PROPERTY_NAME,
            CONSUMER_SERIALIZER_PROPERTY_NAME
    })
    public Consumer kafkaConsumer(){
        Properties props = kafkaConsumerProperties();
        props.put("key.deserializer", "org.apache.kafka.common.serialization.LongDeserializer");
        props.put("value.deserializer", env.getProperty(CONSUMER_SERIALIZER_PROPERTY_NAME));
        KafkaConsumer<Long, Track> kafkaConsumer = new KafkaConsumer<>(props);
        kafkaConsumer.subscribe(Arrays.asList(env.getProperty(CONSUMER_TOPIC_PROPERTY_NAME)));
        return kafkaConsumer;
    }

    private Properties kafkaProducerProperties() {
        Properties props = new Properties();
        props.put("bootstrap.servers", env.getProperty(SERVERS_PROPERTY_NAME));
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        return props;
    }

    private Properties kafkaConsumerProperties() {
        Properties props = new Properties();
        props.put("bootstrap.servers", env.getProperty(SERVERS_PROPERTY_NAME));
        props.put("group.id", "test");
        props.put("enable.auto.commit", "false");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        return props;
    }

}
