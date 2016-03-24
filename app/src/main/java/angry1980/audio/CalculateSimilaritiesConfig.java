package angry1980.audio;

import angry1980.audio.config.KafkaProducerConsumerConfig;
import angry1980.audio.dao.TrackSimilarityDAO;
import angry1980.audio.dao.TrackSimilarityDAOKafkaImpl;
import angry1980.audio.similarity.TracksToCalculate;
import angry1980.audio.similarity.TracksToCalculateKafkaImpl;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Optional;

@Configuration
public class CalculateSimilaritiesConfig {

    @Autowired
    private Optional<Producer> kafkaProducer;
    @Autowired
    private Optional<Consumer> kafkaConsumer;
    @Autowired
    private Environment env;

    @Bean
    @ConditionalOnBean(Producer.class)
    @ConditionalOnProperty(KafkaProducerConsumerConfig.PRODUCER_TOPIC_PROPERTY_NAME)
    public TrackSimilarityDAO trackSimilarityDAO(){
        return new TrackSimilarityDAOKafkaImpl(env.getProperty(KafkaProducerConsumerConfig.PRODUCER_TOPIC_PROPERTY_NAME), kafkaProducer.get());
    }

    @Bean(destroyMethod = "stop")
    @ConditionalOnBean(Consumer.class)
    @ConditionalOnProperty(KafkaProducerConsumerConfig.CONSUMER_ENABLED_PROPERTY_NAME)
    public TracksToCalculate tracksToCalculate(){
        return new TracksToCalculateKafkaImpl(kafkaConsumer.get());
    }


}
