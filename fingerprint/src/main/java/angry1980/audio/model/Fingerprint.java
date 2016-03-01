package angry1980.audio.model;

import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
public interface Fingerprint {

    long getTrackId();

    FingerprintType getType();

    List<TrackHash> getHashes();

}
