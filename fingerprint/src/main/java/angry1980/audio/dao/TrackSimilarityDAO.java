package angry1980.audio.dao;

import angry1980.audio.model.ComparingType;
import angry1980.audio.model.TrackSimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public interface TrackSimilarityDAO extends DAO<TrackSimilarity> {

    Logger LOG = LoggerFactory.getLogger(TrackSimilarityDAO.class);

    default List<TrackSimilarity> findByTrackIdOrEmpty(long trackId){
        return findByTrackId(trackId).orElseGet(() -> Collections.emptyList());
    }

    default Optional<List<TrackSimilarity>> findByTrackId(long trackId){
        return Optional.ofNullable(tryToFindByTrackId(trackId));
    }

    List<TrackSimilarity> tryToFindByTrackId(long trackId);

    default Optional<List<TrackSimilarity>> findByTrackIdAndFingerprintType(long trackId, ComparingType type){
        Optional<List<TrackSimilarity>> result = Optional.of(
                tryToFindByTrackId(trackId).stream()
                        .filter(ts -> ts.getComparingType().equals(type))
                        .collect(Collectors.toList())
        ).filter(list -> !list.isEmpty());
        if(result.isPresent()){
            LOG.debug("There are {} existed similarities of type {}", result.get().size(), type);
        }else {
            LOG.debug("There are not existed similarities of type {}", type);
        }
        return result;
    }

    default Optional<List<TrackSimilarity>> findByFingerprintType(ComparingType type){
        Optional<List<TrackSimilarity>> result = Optional.of(
                tryToGetAll().stream()
                        .filter(ts -> ts.getComparingType().equals(type))
                        .collect(Collectors.toList())
        ).filter(list -> !list.isEmpty());
        if(result.isPresent()){
            LOG.debug("There are {} existed similarities of type {}", result.get().size(), type);
        }else {
            LOG.debug("There are not existed similarities of type {}", type);
        }
        return result;
    }

    Optional<List<TrackSimilarity>> findTruthPositiveByFingerprintType(ComparingType type);

    Optional<List<TrackSimilarity>> findFalsePositiveByFingerprintType(ComparingType type);

    @Override
    default TrackSimilarity tryToGet(long id) {
        throw new UnsupportedOperationException();
    }
}
