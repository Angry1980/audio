package angry1980.audio.service;

import angry1980.audio.stats.FingerprintTypeComparing;
import angry1980.audio.stats.FingerprintTypeResult;
import rx.Observable;

public interface TrackSimilarityStatsService {

    Observable<FingerprintTypeResult> getResultDependsOnFingerprintType();

    Observable<FingerprintTypeComparing> compareFingerprintTypes();
}
