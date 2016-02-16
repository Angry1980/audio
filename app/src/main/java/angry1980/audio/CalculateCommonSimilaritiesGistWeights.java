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
import rx.*;
import rx.Observable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import java.util.stream.Stream;

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
                FingerprintType.CHROMAPRINT//,
                //FingerprintType.LASTFM//,
                //FingerprintType.PEAKS
        )){
            Optional<Interval> best = calculator.calculate(type, 70, false);
            Optional<Interval> bestTP = calculator.calculate(type, 70, true);
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

    public Optional<Interval> calculate(FingerprintType type, int percent, boolean onlyTruthPositive){
        return calculate(() -> function.apply(onlyTruthPositive, type), type).getInterval(percent);
    }

    private Gist calculate(Supplier<Observable<TrackSimilarity>> s, FingerprintType type){
        Gist gist = new Gist(type);
        Object2IntMap<FingerprintType> counter = new Object2IntArrayMap<>(FingerprintType.values().length);
        s.get().subscribe(new Subscriber<TrackSimilarity>() {
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
        return gist;
    }

    private class Gist{

        final FingerprintType type;
        Int2IntSortedMap data;
        int all;

        public Gist(FingerprintType type) {
            this.type = type;
            this.data = new Int2IntAVLTreeMap();
            this.all = 0;
        }

        public Optional<Interval> getInterval(int percent){
            LOG.debug(data.toString());
            int limit = all * percent / 100;
            LOG.debug("Limit is {}", limit);
            return intervals(limit)
                    //looking for an interval with min length
                    .min(Comparator.comparingInt(Interval::getLength))
            ;
        }

        private Stream<Interval> intervals(int limit){
            Function<Integer, Optional<Interval>> fetch = key -> fetchInterval(key, limit);
            return data.keySet().stream()
                    .map(fetch)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .peek(interval -> LOG.debug("Candidate to choose {}", interval))
            ;
        }

        private Optional<Interval> fetchInterval(int key, int limit){
            int sum = 0;
            for(Int2IntMap.Entry entry : data.tailMap(key).int2IntEntrySet()){
                sum += entry.getIntValue();
                if(sum >= limit){
                    return Optional.of(new Interval(key, entry.getIntKey()));
                }
            }
            return Optional.empty();
        }

        public void add(int value){
            data.computeIfPresent(value, (v, c) -> c + 1);
            data.computeIfAbsent(value, v -> 1);
            all++;
        }
    }

    private class Interval{
        final int start;
        final int finish;

        public Interval(int start, int finish) {
            this.start = start;
            this.finish = finish;
        }

        public int getLength(){
            return finish - start;
        }

        @Override
        public String toString() {
            return "Interval{" +
                    "start=" + start +
                    ", finish=" + finish +
                    '}';
        }
    }

}
