package angry1980.audio.kafka;

import angry1980.audio.dao.TrackDAO;
import angry1980.audio.model.ComparingType;
import angry1980.audio.model.Track;
import angry1980.audio.service.TrackSimilarityService;
import angry1980.audio.similarity.TrackSimilarities;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

public class TrackSimilarityConsumer {

    private static Logger LOG = LoggerFactory.getLogger(TrackSimilarityConsumer.class);

    private TrackSimilarityService trackSimilarityService;
    private Consumer<Long, String> kafkaConsumer;
    private final ComparingType comparingType;
    private TrackDAO trackDAO;
    private volatile boolean running = true;

    public TrackSimilarityConsumer(TrackSimilarityService trackSimilarityService,
                                    Consumer<Long, String> kafkaConsumer,
                                    ComparingType comparingType,
                                    TrackDAO trackDAO){
        this.trackSimilarityService = Objects.requireNonNull(trackSimilarityService);
        this.kafkaConsumer = Objects.requireNonNull(kafkaConsumer);
        this.comparingType = Objects.requireNonNull(comparingType);
        this.trackDAO = Objects.requireNonNull(trackDAO);
    }


    public void run(CountDownLatch latch){
        Observable.<Track>create(subscriber -> {
            run(trackId -> trackDAO.get(trackId).ifPresent(track -> subscriber.onNext(track)));
            //subscriber.onCompleted();
        }).doOnNext(track -> LOG.info("Similarity calculation for {}", track))
                .flatMap(track -> trackSimilarityService.findOrCalculateSimilarities(track, comparingType))
                .subscribe(new SubscriberImpl(latch));
    }

    public void stop(){
        this.running = false;
    }

    private void run(java.util.function.Consumer<Long> c){
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

    public class SubscriberImpl extends Subscriber<TrackSimilarities> {

        private CountDownLatch latch;

        public SubscriberImpl(CountDownLatch latch) {
            this.latch = latch;
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
            LOG.info("Consumer will be stopped in a few seconds");
            latch.countDown();
        }

    }


}
