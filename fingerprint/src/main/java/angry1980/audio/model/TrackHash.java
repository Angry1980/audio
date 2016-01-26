package angry1980.audio.model;

import org.immutables.value.Value;

@Value.Immutable
public interface TrackHash {

    long getTrackId();

    int getHash();
}
