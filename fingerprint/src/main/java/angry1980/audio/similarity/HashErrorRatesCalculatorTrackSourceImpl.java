package angry1980.audio.similarity;

import angry1980.audio.dao.TrackDAO;
import angry1980.audio.model.Track;
import rx.Observable;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

public class HashErrorRatesCalculatorTrackSourceImpl implements HashErrorRatesCalculatorTrackSource {

    private boolean all;
    private TrackDAO trackDAO;

    public HashErrorRatesCalculatorTrackSourceImpl(TrackDAO trackDAO) {
        this.trackDAO = Objects.requireNonNull(trackDAO);
        this.all = false;
    }

    public HashErrorRatesCalculatorTrackSourceImpl setAll(boolean all) {
        this.all = all;
        return this;
    }

    @Override
    public Observable<Long> get(long sourceTrackId) {
        return getTracks(sourceTrackId)
                .map(Observable::from)
                .orElseGet(() -> Observable.empty())
                .map(Track::getId)
                .filter(trackId -> trackId != sourceTrackId)
        ;
    }

    private Optional<Collection<Track>> getTracks(long sourceTrackId){
        if(all){
            return trackDAO.getAll();
        }
        return trackDAO.get(sourceTrackId)
                .map(track -> trackDAO.findByCluster(track.getCluster()));
    }
}
