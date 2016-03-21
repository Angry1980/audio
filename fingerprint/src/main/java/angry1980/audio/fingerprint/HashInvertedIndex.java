package angry1980.audio.fingerprint;

import angry1980.audio.dao.TrackHashDAO;
import angry1980.audio.model.*;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongRBTreeSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;

public class HashInvertedIndex implements InvertedIndex<Fingerprint> {

    private static Logger LOG = LoggerFactory.getLogger(HashInvertedIndex.class);

    private TrackHashDAO hashDAO;
    private Optional<Integer> silenceHash;

    public HashInvertedIndex(TrackHashDAO hashDAO, Optional<Integer> silenceHash) {
        this.hashDAO = Objects.requireNonNull(hashDAO);
        this.silenceHash = Objects.requireNonNull(silenceHash);
    }

    @Override
    public Fingerprint save(Fingerprint fingerprint) {
        LOG.debug("Creation of inverted index for {} of type {}", fingerprint.getTrackId(), fingerprint.getType());
        fingerprint.getHashes().stream()
                .forEach(hash -> {
                    try{
                        hashDAO.create(hash);
                    }catch(Exception e){
                        LOG.error("Error while trying to save {} ", hash);
                        LOG.error("", e);
                    }

                });
        return fingerprint;

    }

    /*
        this implementation was optimized for in memory storage
        when using database it will be more efficient to get all similar hashes by one query
     */
    @Override
    public Long2ObjectMap<IntSortedSet> find(Fingerprint fingerprint) {
        Function<Long, IntSortedSet> factory = el -> new IntRBTreeSet();
        Long2ObjectMap<IntSortedSet> map = new Long2ObjectOpenHashMap<>();
        LongSet handled = new LongRBTreeSet();
        for(TrackHash th : fingerprint.getHashes()){
            if(silenceHash.map(v -> v == th.getHash()).orElse(false)){
                continue;
            }
            if(handled.contains(th.getHash())){
                continue;
            }
            for(TrackHash th1 : hashDAO.findByHash(th.getHash())){
                map.computeIfAbsent(th1.getTrackId(), factory).add(th1.getTime());
            }
            handled.add(th.getHash());
        }
        return map;
    }

    //according benchmarks for in memory storage this implementation
    //is almost twice slowly than implementation with iterators
/*
    private Long2ObjectMap<IntSortedSet> findByHashesAndSortByTrack(Collection<TrackHash> hashes) {
        Function<Long, IntSortedSet> factory = el -> new IntRBTreeSet();
        return hashes.stream()
                .mapToLong(TrackHash::getHash)
                .mapToObj(hashDAO::findByHash)
                .flatMap(Collection::stream)
                .collect(
                        Collector.of(
                                () -> new Long2ObjectOpenHashMap<>(),
                                (map, th) -> map.computeIfAbsent(th.getTrackId(), factory).add((int)th.getTime()),
                                (map1, map2) -> {
                                    map2.entrySet().stream()
                                            .filter(entry -> !CollectionUtils.isEmpty(entry.getValue()))
                                            .forEach(entry -> map1.computeIfAbsent(entry.getKey(), factory).addAll(entry.getValue()));
                                    return map1;
                                }
                        )
                );

    }
*/

}
