package angry1980.audio.similarity;

import angry1980.audio.dao.TrackDAO;
import angry1980.audio.model.Track;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

public class HashErrorRatesCalculatorTrackClusterSource implements HashErrorRatesCalculatorTrackSource {

    private TrackDAO trackDAO;

    public HashErrorRatesCalculatorTrackClusterSource(TrackDAO trackDAO) {
        this.trackDAO = Objects.requireNonNull(trackDAO);
    }

    @Override
    public Optional<Collection<Track>> get(long sourceTrackId) {
        return trackDAO.get(sourceTrackId)
                .map(track -> trackDAO.findByCluster(track.getCluster()));
    }
}
