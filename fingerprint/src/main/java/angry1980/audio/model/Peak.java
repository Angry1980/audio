package angry1980.audio.model;

import org.immutables.value.Value;

@Value.Immutable
public interface Peak {

    long getTrackId();
    int getTime();
    long getHash();

}

