package angry1980.audio.service;

import angry1980.audio.model.FingerprintTypeStats;

import java.util.List;

public interface TrackSimilarityStatsService {

    List<FingerprintTypeStats> getFingerprintTypeStats();
}
