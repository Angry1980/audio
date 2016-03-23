package angry1980.audio.similarity;

import angry1980.audio.model.Track;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.*;

public class TracksToCalculateKafkaImpl implements TracksToCalculate {

    private static Logger LOG = LoggerFactory.getLogger(TracksToCalculateKafkaImpl.class);

    private Properties kafkaConsumerProperties;
    private String tracksTopicName;
    private volatile boolean running = true;

    public TracksToCalculateKafkaImpl(Properties kafkaConsumerProperties, String tracksTopicName) {
        this.kafkaConsumerProperties = Objects.requireNonNull(kafkaConsumerProperties);
        this.tracksTopicName = Objects.requireNonNull(tracksTopicName);
    }

    @Override
    public void stop() {
        running = false;
    }

    @Override
    public Observable<Track> get() {
        KafkaConsumer<Long, Track> kafkaConsumer = new KafkaConsumer<>(kafkaConsumerProperties);
        kafkaConsumer.subscribe(Arrays.asList(tracksTopicName));
        return Observable.<Track>create(subscriber -> {
            try{
                run(kafkaConsumer, track -> subscriber.onNext(track));
            } finally {
                subscriber.onCompleted();
                kafkaConsumer.close();
            }
        });
    }

/*
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                consumer.close();
            }
        });
*/
    private void run(KafkaConsumer<Long, Track> kafkaConsumer, java.util.function.Consumer<Track> c){
        while (running) {
            ConsumerRecords<Long, Track> records = kafkaConsumer.poll(Long.MAX_VALUE);
            try{
                for (TopicPartition partition : records.partitions()) {
                    List<ConsumerRecord<Long, Track>> partitionRecords = records.records(partition);
                    for (ConsumerRecord<Long, Track> record : partitionRecords) {
                        LOG.info("offset = {}, key = {}, value = {}", new Object[]{record.offset(), record.key(), record.value()});
                        c.accept(record.value());
                    }
                    long lastoffset = partitionRecords.get(partitionRecords.size() - 1).offset();
                    //todo: user commitAsync
                    kafkaConsumer.commitSync(Collections.singletonMap(partition, new OffsetAndMetadata(lastoffset + 1)));
                }
            } catch (CommitFailedException e) {
                // application specific failure handling
            }
        }
    }

}
