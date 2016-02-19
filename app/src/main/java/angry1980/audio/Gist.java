package angry1980.audio;

import angry1980.audio.model.FingerprintType;
import angry1980.audio.model.TrackSimilarity;
import it.unimi.dsi.fastutil.ints.Int2IntAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntSortedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Gist {

    private static Logger LOG = LoggerFactory.getLogger(Gist.class);

    public static Gist calculate(Supplier<Observable<TrackSimilarity>> s, FingerprintType type){
        Gist gist = new Gist(type);
        Map<FingerprintType, Set<TrackSimilarity>> tracks = new HashMap<>(FingerprintType.values().length);
        s.get().subscribe(new Subscriber<TrackSimilarity>() {
            @Override
            public void onCompleted() {
                tracks.entrySet().stream().forEach(tracks -> LOG.debug("There are {} similarities", tracks.getValue().size()));
            }
            @Override
            public void onError(Throwable throwable) {
                LOG.error("", throwable);
            }
            @Override
            public void onNext(TrackSimilarity trackSimilarity) {
                gist.add(trackSimilarity.getValue());
                tracks.computeIfAbsent(trackSimilarity.getFingerprintType(), ft -> new HashSet<>()).add(trackSimilarity);
            }
        });
        return gist;
    }

    private final FingerprintType type;
    private Int2IntSortedMap data;
    private long all;
    private int max;

    public Gist(FingerprintType type) {
        this.type = type;
        this.data = new Int2IntAVLTreeMap();
        this.all = 0;
        this.max = 0;
    }

    public Optional<Interval> getInterval(int percent){
        LOG.debug(data.toString());
        long limit = all * percent / 100;
        LOG.debug("Limit is {}", limit);
        return intervals(limit)
                //looking for an interval with min length
                .min(Comparator.comparingInt(Interval::getLength))
                ;
    }

    private Stream<Interval> intervals(long limit){
        Function<Integer, Optional<Interval>> fetch = key -> fetchInterval(key, limit);
        return data.keySet().stream()
                .map(fetch)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .peek(interval -> LOG.debug("Candidate to choose {}", interval))
                ;
    }

    private Optional<Interval> fetchInterval(int key, long limit){
        long sum = 0;
        for(Int2IntMap.Entry entry : data.tailMap(key).int2IntEntrySet()){
            sum += getValue(entry);
            if(sum >= limit){
                return Optional.of(new Interval(key, entry.getIntKey()));
            }
        }
        return Optional.empty();
    }

    private long getValue(Int2IntMap.Entry entry){
        //return entry.getValue();
        //todo: normalize key using max
        return entry.getIntKey() * entry.getValue();
    }

    public void add(int value){
        data.computeIfPresent(value, (v, c) -> c + 1);
        data.computeIfAbsent(value, v -> 1);
        fixValue(value);
    }

    private void fixValue(int value){
        //all++;
        all += value;
        max = Math.max(max, value);
    }

    public class Interval {
        final int start;
        final int finish;

        public Interval(int start, int finish) {
            this.start = start;
            this.finish = finish;
        }

        public int getLength() {
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
