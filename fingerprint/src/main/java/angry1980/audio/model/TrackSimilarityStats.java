package angry1980.audio.model;

import org.immutables.value.Value;

@Value.Immutable
public interface TrackSimilarityStats {

    FingerprintType getType();
    int getTruthPositive();
    int getTruthNegative();
    int getFalsePositive();
    int getFalseNegative();
    int getTracksCount();
    int getClustersCount();
    long getBestCluster();
    long getBestTrack();

}
