package angry1980.audio.dao;

import angry1980.audio.model.TrackSimilarity;

import java.util.*;

public class TrackSimilarityDAOInMemoryImpl implements TrackSimilarityDAO {

    private Map<Long, List<TrackSimilarity>> similarities;

    public TrackSimilarityDAOInMemoryImpl() {
        this.similarities = new HashMap<>();
    }

    @Override
    public List<TrackSimilarity> tryToFindByTrackId(long trackId) {
        return similarities.get(trackId);
    }

    @Override
    public Optional<TrackSimilarity> create(TrackSimilarity trackSimilarity) {
        similarities.computeIfAbsent(trackSimilarity.getTrack1(), l -> new ArrayList<>()).add(trackSimilarity);
        similarities.computeIfAbsent(trackSimilarity.getTrack2(), l -> new ArrayList<>()).add(trackSimilarity.reverse());
        return Optional.of(trackSimilarity);
    }
}
