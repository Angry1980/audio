package angry1980.audio.track;

import angry1980.audio.dao.TrackDAO;
import angry1980.audio.dao.TrackSimilarityDAO;
import angry1980.audio.similarity.ImmutableTrackSimilarityCalculatedEvent;
import org.axonframework.eventhandling.annotation.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class StorageEventListener {

    private static Logger LOG = LoggerFactory.getLogger(StorageEventListener.class);

    private TrackDAO trackDAO;
    private TrackSimilarityDAO similarityDAO;

    public StorageEventListener(TrackDAO trackDAO, TrackSimilarityDAO similarityDAO) {
        this.trackDAO = Objects.requireNonNull(trackDAO);
        this.similarityDAO = Objects.requireNonNull(similarityDAO);
    }

    @EventHandler
    public void on(ImmutableTrackCreatedEvent event){
        LOG.debug("Try to save track {}", event.getTrack());
        if(!trackDAO.create(event.getTrack()).isPresent()){
            LOG.warn("{} was not saved to storage", event.getTrack());
        }
    }

    @EventHandler
    public void on(ImmutableTrackSimilarityCalculatedEvent event){
        LOG.debug("Try to save track similarity {}", event.getSimilarity());
        if(!similarityDAO.create(event.getSimilarity()).isPresent()){
            LOG.warn("{} was not saved to storage", event.getSimilarity());
        }

    }
}
