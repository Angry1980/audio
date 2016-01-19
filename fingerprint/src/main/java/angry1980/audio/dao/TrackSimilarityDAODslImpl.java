package angry1980.audio.dao;

import angry1980.audio.dsl.TrackDSL;
import angry1980.audio.model.TrackSimilarity;
import java.util.*;

public class TrackSimilarityDAODslImpl implements TrackSimilarityDAO {

    private TrackDSL trackDSL;

    public TrackSimilarityDAODslImpl(TrackDSL trackDSL) {
        this.trackDSL = Objects.requireNonNull(trackDSL);
    }

    @Override
    public List<TrackSimilarity> tryToFindByTrackId(long trackId) {
        return trackDSL.track(trackId).getSimilarities();
    }

    @Override
    public TrackSimilarity tryToGet(long id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<TrackSimilarity> tryToGetAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TrackSimilarity tryToCreate(TrackSimilarity trackSimilarity) {
        trackDSL.similarity(trackSimilarity)
                .typeOf(trackSimilarity.getFingerprintType())
                .addTrack(trackSimilarity.getTrack1())
                .addTrack(trackSimilarity.getTrack2())
        ;
        return trackSimilarity;
    }

}
