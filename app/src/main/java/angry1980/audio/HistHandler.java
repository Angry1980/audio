package angry1980.audio;

import angry1980.audio.model.FingerprintType;
import angry1980.audio.model.TrackSimilarity;
import angry1980.audio.service.TrackSimilarityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiFunction;

public class HistHandler {

    private static Logger LOG = LoggerFactory.getLogger(HistHandler.class);

    private TrackSimilarityService trackSimilarityService;

    public HistHandler(TrackSimilarityService trackSimilarityService) {
        this.trackSimilarityService = trackSimilarityService;
    }

    public void handle(BiFunction<Boolean, FingerprintType, Observable<TrackSimilarity>> function){
        for(FingerprintType type : Arrays.asList(
                FingerprintType.CHROMAPRINT//,
                //FingerprintType.LASTFM//,
                //FingerprintType.PEAKS
        )){
            Optional<Hist.Interval> bestFP = calculate(function, type, 70, false);
            Optional<Hist.Interval> bestTP = calculate(function, type, 70, true);
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

    private Optional<Hist.Interval> calculate(BiFunction<Boolean, FingerprintType, Observable<TrackSimilarity>> function,
                                              FingerprintType type, int percent, boolean onlyTruthPositive){
        return Hist.calculate(() -> function.apply(onlyTruthPositive, type), type).getInterval(percent);
    }

}
