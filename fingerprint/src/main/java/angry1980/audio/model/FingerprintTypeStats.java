package angry1980.audio.model;

import org.immutables.value.Value;

@Value.Immutable
public interface FingerprintTypeStats {

    FingerprintType getType();
    int getFalsePositive();
    int getFalseNegative();
    int getTruthPositive();
    int getTracksCount();
    int getClustersCount();

}
