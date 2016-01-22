package angry1980.audio.dao;

import angry1980.audio.model.FingerprintType;
import angry1980.audio.model.TrackSimilarity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public interface TrackSimilarityDAO extends DAO<TrackSimilarity> {

    default List<TrackSimilarity> findByTrackIdOrEmpty(long trackId){
        return findByTrackId(trackId).orElseGet(() -> Collections.emptyList());
    }

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

    @Override
    default TrackSimilarity tryToGet(long id) {
        throw new UnsupportedOperationException();
    }
}
