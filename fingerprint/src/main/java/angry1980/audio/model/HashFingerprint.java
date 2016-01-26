package angry1980.audio.model;

import org.immutables.value.Value;

@Value.Immutable
public interface HashFingerprint extends Fingerprint {

    int[] getHashes();
}
