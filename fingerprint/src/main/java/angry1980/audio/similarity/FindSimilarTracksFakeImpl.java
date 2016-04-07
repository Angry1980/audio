package angry1980.audio.similarity;

import angry1980.audio.model.ComparingType;
import angry1980.audio.model.Track;
import angry1980.audio.model.TrackSimilarity;
import org.springframework.core.Ordered;
import rx.Observable;

public class FindSimilarTracksFakeImpl implements FindSimilarTracks {

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public Observable<TrackSimilarity> apply(Track track, ComparingType type) {
        return Observable.empty();
    }

    @Override
    public String toString() {
        return "FindSimilarTracksFakeImpl{}";
    }
}
