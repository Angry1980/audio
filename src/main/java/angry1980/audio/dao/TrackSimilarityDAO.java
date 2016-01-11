package angry1980.audio.dao;

import angry1980.audio.model.FingerprintType;
import angry1980.audio.model.TrackSimilarity;

import java.util.List;
import java.util.Optional;

public interface TrackSimilarityDAO {

    Optional<List<TrackSimilarity>> findByTrackId(long trackId);

    Optional<List<TrackSimilarity>> findByTrackIdAndFingerprintType(long trackId, FingerprintType type);

    Optional<TrackSimilarity> create(TrackSimilarity trackSimilarity);
}
