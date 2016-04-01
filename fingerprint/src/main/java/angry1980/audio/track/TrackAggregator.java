package angry1980.audio.track;

import angry1980.audio.model.Track;
import org.axonframework.commandhandling.annotation.CommandHandler;
import org.axonframework.eventsourcing.annotation.AbstractAnnotatedAggregateRoot;
import org.axonframework.eventsourcing.annotation.AggregateIdentifier;
import org.axonframework.eventsourcing.annotation.EventSourcingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrackAggregator extends AbstractAnnotatedAggregateRoot {

    private static Logger LOG = LoggerFactory.getLogger(TrackAggregator.class);

    @AggregateIdentifier
    private Long trackId;
    private Track track;

    TrackAggregator(){
        //used by axon
    }

    @CommandHandler
    public TrackAggregator(ImmutableCreateTrackCommand command) {
        LOG.info("Creation of track {}", command.getTrack());
        apply(ImmutableTrackCreatedEvent.builder().track(command.getTrack()).build());
    }

    @EventSourcingHandler
    private void on(ImmutableTrackCreatedEvent event){
        this.track = event.getTrack();
        this.trackId = event.getTrack().getId();
    }

}
