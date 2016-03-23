package angry1980.audio.kafka;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class StreamConsumer<K, V> {

    private static Logger LOG = LoggerFactory.getLogger(StreamConsumer.class);

    private volatile boolean running = true;
    private Consumer<K, V> kafkaConsumer;

    public StreamConsumer(Consumer<K, V> kafkaConsumer) {
        this.kafkaConsumer = Objects.requireNonNull(kafkaConsumer);
    }

    public void stop() {
        running = false;
    }

    public Observable<V> get() {
        return Observable.<V>create(subscriber -> {
            try{
                run(kafkaConsumer, track -> subscriber.onNext(track));
            } finally {
                subscriber.onCompleted();
                kafkaConsumer.close();
            }
        });
    }

    private void run(org.apache.kafka.clients.consumer.Consumer<K, V> kafkaConsumer, java.util.function.Consumer<V> c){
        while (running) {
            ConsumerRecords<K, V> records = kafkaConsumer.poll(Long.MAX_VALUE);
            try{
                for (TopicPartition partition : records.partitions()) {
                    List<ConsumerRecord<K, V>> partitionRecords = records.records(partition);
                    for (ConsumerRecord<K, V> record : partitionRecords) {
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
