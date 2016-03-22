package angry1980.audio.kafka;

import angry1980.audio.dao.TrackDAO;
import angry1980.audio.model.ComparingType;
import angry1980.audio.service.TrackSimilarityService;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

@SpringBootApplication
@ComponentScan(value = {"angry1980.audio.config"})
public class ChromaprintConsumer {

    private static Logger LOG = LoggerFactory.getLogger(ChromaprintConsumer.class);

    @Autowired
    private TrackSimilarityService trackSimilarityService;
    @Autowired
    private TrackDAO trackDAO;
    @Autowired
    private Properties kafkaConsumerProperties;

    private volatile boolean running = true;

    public static void main(String[] args){
        SpringApplication sa = new SpringApplication(ChromaprintConsumer.class);
        sa.setAdditionalProfiles(
                "NETFLIX",
                "KAFKA",
                "CALCULATE",
                ComparingType.CHROMAPRINT.name()
        );
        ConfigurableApplicationContext context = sa.run(args);
        context.registerShutdownHook();
        CountDownLatch latch = new CountDownLatch(1);
        LOG.info("Starting application");
        context.getBean(ChromaprintConsumer.class).run(latch);
        try {
            latch.await();
        } catch (InterruptedException e) {
            LOG.error("Error while application working", e);
        }
        context.close();
    }

    public void run(CountDownLatch latch){
        KafkaConsumer<Long, String> consumer = new KafkaConsumer<>(kafkaConsumerProperties);
        consumer.subscribe(Arrays.asList("uploaded-tracks"));
        try{
            new TrackSimilarityConsumer(trackSimilarityService, consumer, ComparingType.CHROMAPRINT, trackDAO)
                                .run(latch);
        } finally {
            consumer.close();
        }

/*
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                consumer.close();
            }
        });
*/



    }

}
