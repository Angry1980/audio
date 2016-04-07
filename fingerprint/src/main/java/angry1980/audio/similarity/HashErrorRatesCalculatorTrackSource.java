package angry1980.audio.similarity;

import rx.Observable;

public interface HashErrorRatesCalculatorTrackSource {

    Observable<Long> get(long sourceTrackId);

}
