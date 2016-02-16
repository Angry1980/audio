package angry1980.audio;

import angry1980.audio.model.FingerprintType;
import angry1980.audio.model.TrackSimilarity;
import angry1980.audio.service.TrackSimilarityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import rx.Observable;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiFunction;

@SpringBootApplication
@Import(Neo4jDAOConfig.class)
public class CalculateSimilaritiesGistWeights {
    private static Logger LOG = LoggerFactory.getLogger(CalculateSimilaritiesGistWeights.class);

    @Autowired
    private TrackSimilarityService trackSimilarityService;
    private BiFunction<Boolean, FingerprintType, Observable<TrackSimilarity>> function =
            (onlyTruthPositive, type) -> trackSimilarityService.findSimilarities(type, onlyTruthPositive);

    public static void main(String[] args){
        SpringApplication sa = new SpringApplication(CalculateSimilaritiesGistWeights.class);
        sa.setAdditionalProfiles(
                FingerprintType.CHROMAPRINT.name(),
                FingerprintType.PEAKS.name(),
                FingerprintType.LASTFM.name(),
                "NEO4J"
        );
        ConfigurableApplicationContext context = sa.run(args);
        context.registerShutdownHook();
        CalculateSimilaritiesGistWeights calculator = context.getBean(CalculateSimilaritiesGistWeights.class);
        for(FingerprintType type : Arrays.asList(
                //FingerprintType.CHROMAPRINT//,
                FingerprintType.LASTFM//,
                //FingerprintType.PEAKS
        )){
            Optional<Gist.Interval> bestFP = calculator.calculate(type, 70, false);
            Optional<Gist.Interval> bestTP = calculator.calculate(type, 70, true);
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

    public Optional<Gist.Interval> calculate(FingerprintType type, int percent, boolean truthPositive){
        return Gist.calculate(() -> function.apply(truthPositive, type), type).getInterval(percent);
    }

}
