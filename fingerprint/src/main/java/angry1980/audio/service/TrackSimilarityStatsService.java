package angry1980.audio.service;

import angry1980.audio.model.FingerprintTypeStats;
import rx.Observable;

public interface TrackSimilarityStatsService {

    Observable<FingerprintTypeStats> getFingerprintTypeStats();
}
