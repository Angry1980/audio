package angry1980.audio.stats;

import angry1980.audio.model.ComparingType;
import angry1980.audio.model.TrackSimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.BiFunction;

public class HistHandler {

    private static Logger LOG = LoggerFactory.getLogger(HistHandler.class);

    private Collection<ComparingType> types;
    private OptionalInt percent;

    public HistHandler(Collection<ComparingType> types) {
        this(types, OptionalInt.empty());
    }

    public HistHandler(Collection<ComparingType> types, OptionalInt percent) {
        this.types = Objects.requireNonNull(types);
        this.percent = Objects.requireNonNull(percent);
    }

    public void handle(BiFunction<Boolean, ComparingType, Observable<TrackSimilarity>> function){
        for(ComparingType type : types){
            Optional<Hist.Interval> bestFP = calculate(function, type, percent.orElse(70), false);
            Optional<Hist.Interval> bestTP = calculate(function, type, percent.orElse(70), true);
            if (bestFP.isPresent()) {
                LOG.info("Best false positive interval for {} is {}", type, bestFP.get());
            } else {
                LOG.warn("It's not possible to calculate best false positive interval for {}", type);
            }
            if (bestTP.isPresent()) {
                LOG.info("Best truth positive interval for {} is {}", type, bestTP.get());
            } else {
                LOG.warn("It's not possible to calculate best truth positive interval for {}", type);
            }

        }
    }

    private Optional<Hist.Interval> calculate(BiFunction<Boolean, ComparingType, Observable<TrackSimilarity>> function,
                                              ComparingType type, int percent, boolean onlyTruthPositive){
        return Hist.calculate(() -> function.apply(onlyTruthPositive, type), type).getInterval(percent);
    }

}
