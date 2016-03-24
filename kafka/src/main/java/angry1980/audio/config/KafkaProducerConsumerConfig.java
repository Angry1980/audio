package angry1980.audio.config;

import angry1980.audio.kafka.ConsumerProperties;
import angry1980.audio.kafka.JsonSerializer;
import angry1980.audio.model.Track;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
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
    public static final String PRODUCER_TOPIC_PROPERTY_NAME = "music.kafka.producer.topic.name";
    public static final String CONSUMER_ENABLED_PROPERTY_NAME = "music.kafka.consumer.enabled";
    public static final String CONSUMER_PROPERTIES = "music.kafka.consumer.properties";

    @Autowired
    private Environment env;

    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(PRODUCER_TOPIC_PROPERTY_NAME)
    public Producer kafkaProducer() {
        Properties props = kafkaProducerProperties();
        props.put("key.serializer", "org.apache.kafka.common.serialization.LongSerializer");
        props.put("value.serializer", JsonSerializer.class.getName());
        return new KafkaProducer<>(props);
    }

    @Bean
    @ConditionalOnProperty(CONSUMER_ENABLED_PROPERTY_NAME)
    public Consumer kafkaConsumer(){
        ConsumerProperties cp = env.getRequiredProperty(CONSUMER_PROPERTIES, ConsumerProperties.class);
        KafkaConsumer<Long, Track> kafkaConsumer = new KafkaConsumer<>(kafkaConsumerProperties(cp));
        kafkaConsumer.subscribe(Arrays.asList(cp.getTopicName()));
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

    private Properties kafkaConsumerProperties(ConsumerProperties cp) {
        Properties props = new Properties();
        props.put("bootstrap.servers", env.getProperty(SERVERS_PROPERTY_NAME));
        props.put("group.id", cp.getGroupName());
        props.put("auto.offset.reset", "earliest");
        props.put("enable.auto.commit", "false");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.LongDeserializer");
        props.put("value.deserializer", cp.getValueDeserializer());
        return props;
    }

}
