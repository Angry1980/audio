package angry1980.audio.model;

import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
public abstract class PeaksFingerprint implements Fingerprint {

    @Override
    public FingerprintType getType() {
        return FingerprintType.PEAKS;
    }

    public abstract List<Peak> getPoints();
}
