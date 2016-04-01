package angry1980.audio.track;

import angry1980.audio.dao.TrackDAO;
import org.axonframework.eventhandling.annotation.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class TrackStorageEventListener {

    private static Logger LOG = LoggerFactory.getLogger(TrackStorageEventListener.class);

    private TrackDAO trackDAO;

    public TrackStorageEventListener(TrackDAO trackDAO) {
        this.trackDAO = Objects.requireNonNull(trackDAO);
    }

    @EventHandler
    public void on(ImmutableTrackCreatedEvent event){
        LOG.debug("Try to save {}", event.getTrack());
        if(!trackDAO.create(event.getTrack()).isPresent()){
            LOG.warn("{} was not saved to storage", event.getTrack());
        }
    }

}
