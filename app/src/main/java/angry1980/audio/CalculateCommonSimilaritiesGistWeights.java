package angry1980.audio;

import angry1980.audio.model.FingerprintType;
import angry1980.audio.model.TrackSimilarity;
import angry1980.audio.service.TrackSimilarityService;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import rx.*;
import rx.Observable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;

@SpringBootApplication
@Import(Neo4jDAOConfig.class)
public class CalculateCommonSimilaritiesGistWeights {

    private static Logger LOG = LoggerFactory.getLogger(CalculateCommonSimilaritiesGistWeights.class);

    @Autowired
    private TrackSimilarityService trackSimilarityService;
    private BiFunction<Boolean, FingerprintType, Observable<TrackSimilarity>> function =
            (onlyTruthPositive, type) -> trackSimilarityService.findCommonSimilarities(type, onlyTruthPositive);

    public static void main(String[] args){
        SpringApplication sa = new SpringApplication(CalculateCommonSimilaritiesGistWeights.class);
        sa.setAdditionalProfiles(
                FingerprintType.CHROMAPRINT.name(),
                FingerprintType.PEAKS.name(),
                FingerprintType.LASTFM.name(),
                "NEO4J"
        );
        ConfigurableApplicationContext context = sa.run(args);
        context.registerShutdownHook();
        CalculateCommonSimilaritiesGistWeights calculator = context.getBean(CalculateCommonSimilaritiesGistWeights.class);
        for(FingerprintType type : Arrays.asList(
                //FingerprintType.CHROMAPRINT//,
                //FingerprintType.LASTFM//,
                FingerprintType.PEAKS
        )){
            Optional<Gist.Interval> best = calculator.calculate(type, 70, false);
            Optional<Gist.Interval> bestTP = calculator.calculate(type, 70, true);
            if (best.isPresent()) {
                LOG.info("Best weight values interval for {} is {}", type, best.get());
            } else {
                LOG.warn("It's not possible to calculate best weight values interval for {}", type);
            }
            if (bestTP.isPresent()) {
                LOG.info("Best truth positive interval for {} is {}", type, bestTP.get());
            } else {
                LOG.warn("It's not possible to calculate best truth positive interval for {}", type);
            }

        }
    }

    public Optional<Gist.Interval> calculate(FingerprintType type, int percent, boolean onlyTruthPositive){
        return Gist.calculate(() -> function.apply(onlyTruthPositive, type), type).getInterval(percent);
    }

}
