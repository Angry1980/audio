package angry1980.audio.dao;

import angry1980.audio.model.ComparingType;
import angry1980.audio.model.TrackSimilarity;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.*;

public class TrackSimilarityDAOKafkaImpl implements TrackSimilarityDAO {

    private String topic;
    private Producer<Long, TrackSimilarity> producer;

    public TrackSimilarityDAOKafkaImpl(String topic, Producer<Long, TrackSimilarity> producer) {
        this.topic = Objects.requireNonNull(topic);
        this.producer = Objects.requireNonNull(producer);
    }

    @Override
    public List<TrackSimilarity> tryToFindByTrackId(long trackId) {
        return Collections.emptyList();
    }

    @Override
    public Optional<List<TrackSimilarity>> findTruthPositiveByFingerprintType(ComparingType type) {
        return Optional.empty();
    }

    @Override
    public Optional<List<TrackSimilarity>> findFalsePositiveByFingerprintType(ComparingType type) {
        return Optional.empty();
    }

    @Override
    public Collection<TrackSimilarity> tryToGetAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TrackSimilarity tryToCreate(TrackSimilarity entity) {
        try{
            producer.send(new ProducerRecord<>(topic, entity.getTrack1(), entity));
            LOG.info("Sending {} to kafka is finished", entity);
        } catch (Exception e){
            LOG.error(e.getMessage());
        }
        return entity;
    }
}
