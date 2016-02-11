package angry1980.audio.fingerprint;

import angry1980.audio.dao.TrackHashDAO;
import angry1980.audio.model.Fingerprint;
import angry1980.audio.model.ImmutableTrackSimilarity;
import angry1980.audio.model.TrackHash;
import angry1980.audio.model.TrackSimilarity;
import angry1980.utils.Numbered;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HashInvertedIndex implements InvertedIndex<Fingerprint>, angry1980.audio.similarity.Calculator<Fingerprint>{

    private static Logger LOG = LoggerFactory.getLogger(HashInvertedIndex.class);

    private int filterWeight;
    private int minWeight;
    private TrackHashDAO hashDAO;

    public HashInvertedIndex(int filterWeight, int minWeight, TrackHashDAO hashDAO) {
        this.filterWeight = filterWeight;
        this.minWeight = minWeight;
        this.hashDAO = Objects.requireNonNull(hashDAO);
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
    public List<TrackSimilarity> calculate(Fingerprint fingerprint) {
        LOG.debug("Similarity calculation for {} of type {}", fingerprint.getTrackId(), fingerprint.getType());
        Supplier<SortedSet<TrackHash>> supplier = () -> new TreeSet<TrackHash>(Comparator.comparingInt(TrackHash::getTime));
        Map<Long, SortedSet<TrackHash>> temp = fingerprint.getHashes().stream()
                //.peek(h -> LOG.debug("Check hashes for {}", h))
                .map(hash -> hashDAO.findByHash(hash.getHash()))
                .flatMap(list -> list.stream())
                .filter(th -> fingerprint.getTrackId() != th.getTrackId())
                .collect(
                        Collectors.groupingBy(TrackHash::getTrackId, Collectors.toCollection(supplier))
                );
        LOG.debug("There are {} similarity candidates for {} of type {} ", new Object[]{temp.size(), fingerprint.getTrackId(), fingerprint.getType()});
        return temp.entrySet().stream()
                //.peek(entry -> LOG.debug("Results by track {}", entry))
                .map(entry -> new Numbered<>(entry.getKey(), this.splitAndSum(entry.getValue(), filterWeight)))
                .filter(n -> n.getValue() > minWeight)
                .map(n -> ImmutableTrackSimilarity.builder()
                        .track1(fingerprint.getTrackId())
                        .track2(n.getNumber())
                        .fingerprintType(fingerprint.getType())
                        .value(n.getValue())
                        .build()
                ).peek(ts -> LOG.debug("{} was created", ts))
                .collect(Collectors.toList());
    }

    private int splitAndSum(Set<TrackHash> data, int filterWeight){
        int result = 0;
        int prevTime = 0;
        int current = 0;
        for(TrackHash th : data){
            if(th.getTime() != 0 && prevTime != th.getTime() - 1){
                if(current > filterWeight){
                    result += current;
                }
                current = 0;
            }
            prevTime = th.getTime();
            current++;
        }
        if(current > filterWeight){
            result += current;
        }
        return result;
    }

}
