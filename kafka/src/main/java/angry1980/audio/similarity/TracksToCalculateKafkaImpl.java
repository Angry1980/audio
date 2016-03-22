package angry1980.audio.similarity;

import angry1980.audio.dao.TrackDAO;
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
    private TrackDAO trackDAO;
    private volatile boolean running = true;

    public TracksToCalculateKafkaImpl(TrackDAO trackDAO, Properties kafkaConsumerProperties, String tracksTopicName) {
        this.kafkaConsumerProperties = Objects.requireNonNull(kafkaConsumerProperties);
        this.tracksTopicName = Objects.requireNonNull(tracksTopicName);
        this.trackDAO = Objects.requireNonNull(trackDAO);
    }

    @Override
    public void stop() {
        running = false;
    }

    @Override
    public Observable<Track> get() {
        KafkaConsumer<Long, String> kafkaConsumer = new KafkaConsumer<>(kafkaConsumerProperties);
        kafkaConsumer.subscribe(Arrays.asList(tracksTopicName));
        return Observable.<Track>create(subscriber -> {
            try{
                //todo: value as track asap
                run(kafkaConsumer, trackId -> trackDAO.get(trackId).ifPresent(track -> subscriber.onNext(track)));
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
    private void run(KafkaConsumer<Long, String> kafkaConsumer, java.util.function.Consumer<Long> c){
        while (running) {
            ConsumerRecords<Long, String> records = kafkaConsumer.poll(Long.MAX_VALUE);
            try{
                for (TopicPartition partition : records.partitions()) {
                    List<ConsumerRecord<Long, String>> partitionRecords = records.records(partition);
                    for (ConsumerRecord<Long, String> record : partitionRecords) {
                        LOG.info("offset = {}, key = {}, value = {}", new Object[]{record.offset(), record.key(), record.value()});
                        c.accept(record.key());
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
