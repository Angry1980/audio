package angry1980.audio.track;

import angry1980.audio.model.Track;
import org.immutables.value.Value;

@Value.Immutable
public interface TrackCreatedEvent {

    Track getTrack();
}
