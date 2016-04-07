package angry1980.audio.similarity;

import angry1980.audio.model.ComparingType;
import angry1980.audio.service.TrackSimilarityService;
import org.springframework.context.ApplicationContext;
import rx.Observable;

import java.util.Objects;
import java.util.Optional;

public class HashErrorRatesCalculatorTrackSourceProxy implements HashErrorRatesCalculatorTrackSource {

    private ApplicationContext context;
    private boolean inited;
    private ComparingType type;
    private Optional<HashErrorRatesCalculatorTrackSource> trackSource;

    public HashErrorRatesCalculatorTrackSourceProxy(ApplicationContext context, ComparingType type) {
        this.context = Objects.requireNonNull(context);
        this.type = Objects.requireNonNull(type);
        this.inited = false;
    }

    @Override
    public Observable<Long> get(long sourceTrackId) {
        return getTrackSource().map(ts -> ts.get(sourceTrackId)).orElseGet(() -> Observable.empty());
    }

    private Optional<HashErrorRatesCalculatorTrackSource> getTrackSource(){
        if(!inited){
            trackSource = Optional.ofNullable(context.getBean(TrackSimilarityService.class))
                            .map(s -> new HashErrorRatesCalculatorSimilarTrackSource(s, type));
            inited = true;
        }
        return trackSource;
    }

}
