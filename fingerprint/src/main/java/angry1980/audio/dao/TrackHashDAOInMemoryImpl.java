package angry1980.audio.dao;

import angry1980.audio.model.TrackHash;
import it.unimi.dsi.fastutil.longs.Long2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectRBTreeMap;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.LongStream;

public class TrackHashDAOInMemoryImpl implements TrackHashDAO {

    private static Comparator<TrackHash> c = Comparator.comparingInt(TrackHash::getTime);

    private final long mask;
    private final Long2ObjectMap<Collection<TrackHash>> index;

    public TrackHashDAOInMemoryImpl(){
        this(16, -1);
    }

    public TrackHashDAOInMemoryImpl(long mask){
        this(16, mask);
    }

    public TrackHashDAOInMemoryImpl(int expectedSize, long mask){
        this.index = new Long2ObjectOpenHashMap<>(expectedSize);
        this.mask = mask;
    }

    @Override
    public Optional<TrackHash> create(TrackHash hash) {
        if(hash != null){
            index.computeIfAbsent(getKey(hash.getHash()), l -> new ArrayList<>()).add(hash);
        }
        return Optional.ofNullable(hash);
    }

    @Override
    public Collection<TrackHash> findByHash(long hash) {
        return index.getOrDefault(getKey(hash), Collections.emptyList());
    }

    @Override
    public Long2ObjectMap<SortedSet<TrackHash>> findByHashesAndSortByTrack(Collection<TrackHash> hashes) {
        return findByHashesAndSortByTrack(hashes.stream().mapToLong(TrackHash::getHash));
    }

    @Override
    public Long2ObjectMap<SortedSet<TrackHash>> findByHashesAndSortByTrack(long[] hashes) {
        return findByHashesAndSortByTrack(Arrays.stream(hashes));
    }

    private Long2ObjectMap<SortedSet<TrackHash>> findByHashesAndSortByTrack(LongStream hashes) {
        Function<Long, SortedSet<TrackHash>> factory = el -> new TreeSet<>(c);
        return hashes
                .mapToObj(this::findByHash)
                .flatMap(Collection::stream)
                .collect(
                        Collector.of(
                                () -> new Long2ObjectOpenHashMap<>(),
                                (map, th) -> map.computeIfAbsent(th.getTrackId(), factory).add(th),
                                (map1, map2) -> {
                                    map2.entrySet().stream()
                                            .filter(entry -> !CollectionUtils.isEmpty(entry.getValue()))
                                            .forEach(entry -> map1.computeIfAbsent(entry.getKey(), factory).addAll(entry.getValue()));
                                    return map1;
                                }
                        )
                );

    }

    private long getKey(long hash){
        if(mask == -1){
            return hash;
        }
        return hash & mask;
    }
}
