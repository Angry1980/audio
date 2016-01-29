package angry1980.audio.service;

import angry1980.audio.stats.FingerprintTypeComparing;
import angry1980.audio.stats.FingerprintTypeResult;
import rx.Observable;

import java.util.List;
import java.util.Map;

public interface TrackSimilarityStatsService {

    Observable<FingerprintTypeResult> getResultDependsOnFingerprintType();

    Observable<FingerprintTypeComparing> compareFingerprintTypes();

    Map<Long, List<Long>> generateClusters();
}
