package angry1980.audio.stats;

import angry1980.audio.model.FingerprintType;
import org.immutables.value.Value;

@Value.Immutable
public interface FingerprintTypeResult {

    FingerprintType getType();
    int getFalsePositive();
    int getFalseNegative();
    int getTruthPositive();
    int getTracksCount();
    int getClustersCount();

}
