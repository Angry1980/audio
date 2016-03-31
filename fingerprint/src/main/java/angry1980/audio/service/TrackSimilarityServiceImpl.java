package angry1980.audio.service;

import angry1980.audio.dao.TrackDAO;
import angry1980.audio.dao.TrackSimilarityDAO;
import angry1980.audio.model.ComparingType;
import angry1980.audio.model.Track;
import angry1980.audio.model.TrackSimilarity;
import angry1980.audio.similarity.FindSimilarTracks;
import angry1980.audio.similarity.TracksToCalculate;
import it.unimi.dsi.fastutil.longs.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import rx.Observable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class TrackSimilarityServiceImpl implements TrackSimilarityService {

    private static Logger LOG = LoggerFactory.getLogger(TrackSimilarityServiceImpl.class);

    private TrackDAO trackDAO;
    private TrackSimilarityDAO trackSimilarityDAO;
    private FindSimilarTracks findSimilarTracks;
    private TracksToCalculate tracksToCalculate;

    public TrackSimilarityServiceImpl(FindSimilarTracks findSimilarTracks,
                                      TrackDAO trackDAO,
                                      TrackSimilarityDAO trackSimilarityDAO,
                                      TracksToCalculate tracksToCalculate) {
        this.trackDAO = Objects.requireNonNull(trackDAO);
        this.trackSimilarityDAO = Objects.requireNonNull(trackSimilarityDAO);
        this.findSimilarTracks = Objects.requireNonNull(findSimilarTracks);
        this.tracksToCalculate = Objects.requireNonNull(tracksToCalculate);
    }

    @Override
    public Observable<Track> getTracksToCalculateSimilarity() {
        return tracksToCalculate.get();
    }

    @Override
    public Observable<TrackSimilarity> findOrCalculateSimilarities(long trackId, ComparingType... types) {
        return trackDAO.get(trackId)
                .map(track -> this.findOrCalculateSimilarities(track, types))
                .orElseGet(() -> Observable.empty());
    }

    @Override
    public Observable<TrackSimilarity> findOrCalculateSimilarities(Track track, ComparingType... types) {
        return Observable.from(types)
                .doOnNext(t -> LOG.debug("{} is getting ready to handle by {} implementation", track.getId(), t))
                .flatMap(t -> findSimilarTracks.apply(track, t));
    }

    @Override
    public Observable<TrackSimilarity> findSimilarities(ComparingType comparingType, boolean truthPositive) {
        Supplier<Optional<List<TrackSimilarity>>> s = () -> truthPositive
                ? trackSimilarityDAO.findTruthPositiveByFingerprintType(comparingType)
                : trackSimilarityDAO.findFalsePositiveByFingerprintType(comparingType)
        ;
        return Observable.create(subscriber -> {
            s.get().orElseGet(Collections::emptyList)
                    .stream()
                    .filter(Objects::nonNull)
                    .forEach(subscriber::onNext)
            ;
            subscriber.onCompleted();
        });
    }

    @Override
    public Observable<TrackSimilarity> findCommonSimilarities(ComparingType comparingType, boolean onlyTruthPositive) {
        Long2ObjectMap<TrackSimilarity> empty = new Long2ObjectArrayMap<>();
        Function<ComparingType, Optional<List<TrackSimilarity>>> f = t -> onlyTruthPositive
                                                ? trackSimilarityDAO.findTruthPositiveByFingerprintType(t)
                                                : trackSimilarityDAO.findByFingerprintType(t)
                                                //: trackSimilarityDAO.findFalsePositiveByFingerprintType(t)
        ;
        Long2ObjectMap<Long2ObjectMap<TrackSimilarity>> sorted = f.apply(comparingType)
                .map(this::sortByTracks)
                .orElseGet(Long2ObjectArrayMap::new);
        return Observable.create(subscriber -> {
            Arrays.stream(ComparingType.values())
                    .filter(type -> !type.equals(comparingType))
                    .flatMap(type -> f.apply(type).orElseGet(Collections::emptyList).stream())
                    .map(ts -> sorted.getOrDefault(ts.getTrack1(), empty).get(ts.getTrack2()))
                    .filter(Objects::nonNull)
                    .forEach(subscriber::onNext)
            ;
            subscriber.onCompleted();
        });
    }

    @Override
    public Observable<TrackSimilarity> findUniqueSimilarities(ComparingType comparingType, boolean onlyTruthPositive) {
        Long2ObjectMap<TrackSimilarity> empty = new Long2ObjectArrayMap<>();
        Function<ComparingType, Optional<List<TrackSimilarity>>> f = t -> onlyTruthPositive
                ? trackSimilarityDAO.findTruthPositiveByFingerprintType(t)
                //: trackSimilarityDAO.findByFingerprintType(t)
                : trackSimilarityDAO.findFalsePositiveByFingerprintType(t)
                ;
        Long2ObjectMap<Long2ObjectMap<TrackSimilarity>>[] sorted = Arrays.stream(ComparingType.values())
                .filter(type -> !type.equals(comparingType))
                .map(type -> f.apply(type).map(this::sortByTracks).orElseGet(Long2ObjectArrayMap::new))
                .toArray(Long2ObjectMap[]::new);
        return Observable.create(subscriber -> {
            f.apply(comparingType).orElseGet(Collections::emptyList).stream()
                    .filter(ts -> {
                        for(Long2ObjectMap<Long2ObjectMap<TrackSimilarity>> s : sorted){
                            if(s.getOrDefault(ts.getTrack1(), empty).containsKey(ts.getTrack2())){
                                return false;
                            }
                        }
                        return true;
                    }).forEach(subscriber::onNext)
            ;
            subscriber.onCompleted();
        });

    }

    private Long2ObjectMap<Long2ObjectMap<TrackSimilarity>> sortByTracks(List<TrackSimilarity> list){
        return list.stream().collect(
                Collector.of(
                        () -> new Long2ObjectArrayMap<Long2ObjectMap<TrackSimilarity>>(),
                        (map, ts) -> map.computeIfAbsent(ts.getTrack1(), t1 -> new Long2ObjectArrayMap<>()).put(ts.getTrack2(), ts),
                        (map1, map2) -> {
                            map2.entrySet().stream()
                                    .filter(entry -> !CollectionUtils.isEmpty(entry.getValue()))
                                    .forEach(entry -> map1.computeIfAbsent(entry.getKey(), k -> new Long2ObjectArrayMap<>()).putAll(entry.getValue()));
                            return map1;
                        }
                )
        );
    }
}
