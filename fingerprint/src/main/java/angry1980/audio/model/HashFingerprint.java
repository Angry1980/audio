package angry1980.audio.model;

import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
public interface HashFingerprint extends Fingerprint {

    List<TrackHash> getHashes();
}
