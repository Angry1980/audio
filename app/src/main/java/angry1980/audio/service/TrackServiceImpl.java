package angry1980.audio.service;

import angry1980.audio.dao.TrackDAO;
import angry1980.audio.model.Track;
import rx.Observable;

import java.util.Objects;

public class TrackServiceImpl implements TrackService{

    private TrackDAO trackDAO;

    public TrackServiceImpl(TrackDAO trackDAO) {
        this.trackDAO = Objects.requireNonNull(trackDAO);
    }

    @Override
    public Observable<Track> getTracksToCalculateSimilarity() {
        return Observable.from(trackDAO.getAllOrEmpty());
    }
}
