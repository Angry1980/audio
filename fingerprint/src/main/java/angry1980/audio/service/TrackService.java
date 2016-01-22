package angry1980.audio.service;

import angry1980.audio.model.Track;
import rx.Observable;

public interface TrackService {

    Observable<Track> getTracksToCalculateSimilarity();

}
