package angry1980.audio.service;

import angry1980.audio.model.FingerprintType;
import angry1980.audio.stats.Stats;
import rx.Observable;

import java.util.List;
import java.util.Map;

public interface TrackSimilarityStatsService {

    Observable<Stats> compareFingerprintTypes();

    Stats getResultDependsOnFingerprintType(FingerprintType type, int minWeight);

    Map<Long, List<Long>> generateClusters();
}
