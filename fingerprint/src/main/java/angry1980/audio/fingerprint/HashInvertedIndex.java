package angry1980.audio.fingerprint;

import angry1980.audio.dao.TrackHashDAO;
import angry1980.audio.model.*;
import angry1980.audio.similarity.Calculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class HashInvertedIndex implements InvertedIndex<HashFingerprint>, Calculator<HashFingerprint> {

    private static Logger LOG = LoggerFactory.getLogger(HashInvertedIndex.class);

    private int filterWeight;
    private int minWeight;
    private TrackHashDAO hashDAO;

    public HashInvertedIndex(TrackHashDAO hashDAO) {
        this(hashDAO, 10, 10);
    }

    public HashInvertedIndex(TrackHashDAO hashDAO, int minWeight, int filterWeight) {
        this.hashDAO = Objects.requireNonNull(hashDAO);
        this.minWeight = minWeight;
        this.filterWeight = filterWeight;
    }

    @Override
    public HashFingerprint save(HashFingerprint fingerprint) {
        LOG.debug("Creation of inverted index for {}", fingerprint.getTrackId());
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
    public List<TrackSimilarity> calculate(HashFingerprint fingerprint) {
        //todo: refactor, same code as in PeaksInvertedIndex
        LOG.debug("Similarity calculation for {}", fingerprint.getTrackId());
        Supplier<Set<TrackHash>> supplier = () -> new TreeSet<>(Comparator.comparingInt(TrackHash::getTime));
        Map<Long, Set<TrackHash>> temp = fingerprint.getHashes().stream()
                //.peek(h -> LOG.debug("Check hashes for {}", h))
                .map(hash -> hashDAO.findByHash(hash.getHash()))
                .flatMap(list -> list.stream())
                .filter(th -> fingerprint.getTrackId() != th.getTrackId())
                .collect(
                        Collectors.groupingBy(TrackHash::getTrackId, Collectors.toCollection(supplier))
                );
        return temp.entrySet().stream()
                //.peek(entry -> LOG.debug("Results by track {}", entry))
                .map(entry -> InvertedIndex.reduceTrackSimilarity(
                                    fingerprint,
                                    entry.getKey(),
                                    this.split(entry.getValue()).stream()
                                        .filter(set -> set.size() > filterWeight)
                                        //.peek(set -> LOG.debug("{} was passed by filterWeight {}", set))
                                        .map(set -> (long)set.size())
                                    )
                )
                .filter(ts -> ts.getValue() > minWeight)
                .collect(Collectors.toList());
    }

    private List<Set<TrackHash>> split(Set<TrackHash> data){
        List<Set<TrackHash>> result = new ArrayList<>();
        Set<TrackHash> current = new HashSet<>();
        int prev = 0;
        for(TrackHash th : data){
            if(th.getTime() != 0 && prev != th.getTime() - 1){
                result.add(current);
                current = new HashSet<>();
            }
            prev = th.getTime();
            current.add(th);
        }
        result.add(current);
        return result;
    }

}
