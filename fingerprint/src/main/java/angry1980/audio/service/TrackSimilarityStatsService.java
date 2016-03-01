package angry1980.audio.service;

import angry1980.audio.model.ComparingType;
import angry1980.audio.stats.Stats;
import rx.Observable;

import java.util.List;
import java.util.Map;

public interface TrackSimilarityStatsService {

    Observable<Stats> compareFingerprintTypes(Map<ComparingType, Integer> minWeights);

    Stats getResultDependsOnFingerprintType(ComparingType type, int minWeight);

    Map<Long, List<Long>> generateClusters(Map<ComparingType, Integer> minWeights);
}
