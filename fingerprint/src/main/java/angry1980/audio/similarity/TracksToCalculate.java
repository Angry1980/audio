package angry1980.audio.similarity;

import angry1980.audio.model.Track;
import rx.Observable;

public interface TracksToCalculate {

    Observable<Track> get();

    default void stop(){
    }

}
