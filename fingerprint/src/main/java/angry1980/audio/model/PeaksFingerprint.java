package angry1980.audio.model;

import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
public abstract class PeaksFingerprint implements Fingerprint {

    public static PeaksFingerprint build(long trackId, List<Peak> peaks){
        return ImmutablePeaksFingerprint.builder()
                    .trackId(trackId)
                    .points(peaks)
                        .build();
    }

    @Override
    public FingerprintType getType() {
        return FingerprintType.PEAKS;
    }

    public abstract List<Peak> getPoints();
}
