package angry1980.audio.dao;

import angry1980.audio.model.FingerprintType;
import angry1980.audio.model.TrackSimilarity;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public interface TrackSimilarityDAO {

    default Optional<List<TrackSimilarity>> findByTrackId(long trackId){
        return Optional.ofNullable(tryToFindByTrackId(trackId));
    }

    List<TrackSimilarity> tryToFindByTrackId(long trackId);

    default Optional<List<TrackSimilarity>> findByTrackIdAndFingerprintType(long trackId, FingerprintType type){
        return Optional.of(
                tryToFindByTrackId(trackId).stream()
                        .filter(ts -> ts.getFingerprintType().equals(type))
                        .collect(Collectors.toList())
        ).filter(list -> !list.isEmpty());
    }

    Optional<TrackSimilarity> create(TrackSimilarity trackSimilarity);

}
