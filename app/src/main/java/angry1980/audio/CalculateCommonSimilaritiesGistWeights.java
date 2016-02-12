package angry1980.audio;

import angry1980.audio.model.FingerprintType;
import angry1980.audio.model.TrackSimilarity;
import angry1980.audio.service.TrackSimilarityService;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import rx.Subscriber;

import java.util.*;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

import java.util.stream.Stream;

@SpringBootApplication
@Import(Neo4jDAOConfig.class)
public class CalculateCommonSimilaritiesGistWeights {

    private static Logger LOG = LoggerFactory.getLogger(CalculateCommonSimilaritiesGistWeights.class);

    @Autowired
    private TrackSimilarityService trackSimilarityService;

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
                FingerprintType.CHROMAPRINT//,
                //FingerprintType.LASTFM//,
                //FingerprintType.PEAKS
        )){
            int[] best = calculator.calculate(type)
                    .flatMap(gist -> gist.getMaxInterval(10))
                    .orElse(null);
            if (best != null) {
                LOG.info("Best weight values interval for {} is {}", type, Arrays.toString(best));
            } else {
                LOG.warn("It's not possible to calculate best interval for {}", type);
            }
        }
    }

    public Optional<Gist> calculate(FingerprintType type){
        Gist gist = new Gist(type);
        Object2IntMap<FingerprintType> counter = new Object2IntArrayMap<>(FingerprintType.values().length);
        trackSimilarityService.findCommonSimilarities(type)
                //.concatWith()
                .subscribe(new Subscriber<TrackSimilarity>() {
                    @Override
                    public void onCompleted() {
                        counter.entrySet().stream().map(Object::toString).forEach(LOG::debug);
                    }
                    @Override
                    public void onError(Throwable throwable) {
                        LOG.error("", throwable);
                    }
                    @Override
                    public void onNext(TrackSimilarity trackSimilarity) {
                        gist.add(trackSimilarity.getValue());
                        counter.computeIfPresent(trackSimilarity.getFingerprintType(), (ft, c) -> c + 1);
                        counter.computeIfAbsent(trackSimilarity.getFingerprintType(), ft -> 1);
                    }
                });
        return Optional.of(gist);
    }

    private class Gist{

        final FingerprintType type;
        Int2IntSortedMap data;

        public Gist(FingerprintType type) {
            this.type = type;
            this.data = new Int2IntAVLTreeMap();
        }

        public Optional<int[]> getMaxInterval(int count){
            LOG.debug(data.toString());
            return intervals(count)
                    .max(Comparator.comparingInt(records -> records.stream().mapToInt(Map.Entry::getValue).sum()))
                    .map(records -> records.stream().mapToInt(Map.Entry::getKey).toArray())
            ;
        }

        private Stream<List<Map.Entry<Integer, Integer>>> intervals(int count){
            IntFunction<List<Map.Entry<Integer, Integer>>> fetch = key -> fetchInterval(key, count);
            return data.keySet().stream()
                    .mapToInt(k -> k)
                    .mapToObj(fetch)
                    .peek(records -> LOG.debug("Candidate to choose {}", records))
            ;

        }

        private List<Map.Entry<Integer, Integer>> fetchInterval(int key, int count){
            return data.tailMap(key).entrySet().stream().limit(count).collect(Collectors.toList());
        }

        public void add(int value){
            data.computeIfPresent(value, (v, c) -> c + 1);
            data.computeIfAbsent(value, v -> 1);
        }
    }
}
