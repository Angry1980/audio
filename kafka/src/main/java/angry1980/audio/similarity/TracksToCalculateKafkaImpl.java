package angry1980.audio.similarity;

import angry1980.audio.kafka.StreamConsumer;
import angry1980.audio.model.Track;
import org.apache.kafka.clients.consumer.*;
import rx.Observable;

public class TracksToCalculateKafkaImpl implements TracksToCalculate {

    private StreamConsumer<Long, Track> consumer;

    public TracksToCalculateKafkaImpl(Consumer<Long, Track> kafkaConsumer) {
        consumer = new StreamConsumer<>(kafkaConsumer);
    }

    @Override
    public void stop() {
        consumer.stop();
    }

    @Override
    public Observable<Track> get() {
        return consumer.get();
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
