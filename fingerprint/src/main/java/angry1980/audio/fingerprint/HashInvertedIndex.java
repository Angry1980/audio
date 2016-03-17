package angry1980.audio.fingerprint;

import angry1980.audio.dao.TrackHashDAO;
import angry1980.audio.model.*;
import angry1980.utils.Numbered;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.longs.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class HashInvertedIndex implements InvertedIndex<Fingerprint>, angry1980.audio.similarity.Calculator<Fingerprint>{

    private static Logger LOG = LoggerFactory.getLogger(HashInvertedIndex.class);

    private double filterWeightPercent;
    private double minWeightPercent;
    private TrackHashDAO hashDAO;
    private Optional<Integer> silenceHash = Optional.empty();

    public HashInvertedIndex(double filterWeightPercent, double minWeightPercent, TrackHashDAO hashDAO) {
        this.filterWeightPercent = filterWeightPercent;
        this.minWeightPercent = minWeightPercent;
        this.hashDAO = Objects.requireNonNull(hashDAO);
    }

    public HashInvertedIndex setSilenceHash(int silenceHash) {
        this.silenceHash = Optional.of(silenceHash);
        return this;
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

    @Override
    public boolean test(SimilarityType similarityType) {
        return SimilarityType.MASKED.equals(similarityType);
    }

    @Override
    public List<TrackSimilarity> calculate(Fingerprint fingerprint, ComparingType comparingType) {
        LOG.debug("Similarity calculation for {} of type {}", fingerprint.getTrackId(), fingerprint.getType());
        Long2ObjectMap<IntSortedSet> hashes = findByHashesAndSortByTrack(fingerprint.getHashes());
        LOG.debug("There are {} similarity candidates for {} of type {} ", new Object[]{hashes.size(), fingerprint.getTrackId(), fingerprint.getType()});
        int minWeight = (int) Math.floor(fingerprint.getHashes().size() * minWeightPercent);
        int filterWeight = (int) Math.floor(fingerprint.getHashes().size() * filterWeightPercent);;
        return hashes.entrySet().stream()
                //.peek(entry -> LOG.debug("Results by track {}", entry))
                .filter(entry -> !entry.getKey().equals(fingerprint.getTrackId()))
                .map(entry -> new Numbered<>(entry.getKey(), this.splitAndSum(entry.getValue(), filterWeight)))
                .filter(n -> n.getValue() > minWeight)
                .map(n -> ImmutableTrackSimilarity.builder()
                        .track1(fingerprint.getTrackId())
                        .track2(n.getNumber())
                        .comparingType(comparingType)
                        .value(n.getValue())
                        .build()

                ).peek(ts -> LOG.debug("{} was created", ts))
                .collect(Collectors.toList());
    }

    private int splitAndSum(IntSortedSet data, int filterWeight){
        if(data.size() < filterWeight){
            return 0;
        }
        int result = 0;
        int prevTime = 0;
        int current = 0;
        for(int time : data){
            if(time != 0 && prevTime != time - 1){
                if(current > filterWeight){
                    result += current;
                }
                current = 0;
            }
            prevTime = time;
            current++;
        }
        if(current > filterWeight){
            result += current;
        }
        return result;
    }

    /*
        this implementation was optimized for in memory storage
        when using database it will be more efficient to get all similar hashes by one query
     */
    private Long2ObjectMap<IntSortedSet> findByHashesAndSortByTrack(Collection<TrackHash> hashes) {
        Function<Long, IntSortedSet> factory = el -> new IntRBTreeSet();
        Long2ObjectMap<IntSortedSet> map = new Long2ObjectOpenHashMap<>();
        LongSet handled = new LongRBTreeSet();
        for(TrackHash th : hashes){
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
