package angry1980.audio.fingerprint;

import angry1980.audio.dao.TrackHashDAO;
import angry1980.audio.model.Fingerprint;
import angry1980.audio.model.ImmutableTrackSimilarity;
import angry1980.audio.model.TrackHash;
import angry1980.audio.model.TrackSimilarity;
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
        Supplier<Set<TrackHash>> supplier = () -> new TreeSet<TrackHash>(Comparator.comparingInt(TrackHash::getTime));
        Map<Long, Set<TrackHash>> temp = fingerprint.getHashes().stream()
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
                .map(entry -> reduceTrackSimilarity(
                        fingerprint,
                        entry.getKey(),
                        this.split(entry.getValue()).stream()
                                .filter(set -> set.size() > filterWeight)
                                //.peek(set -> LOG.debug("{} was passed by filterWeight {}", set))
                                .map(set -> (long)set.size())
                        )
                )
                .filter(ts -> ts.getValue() > minWeight)
                .peek(ts -> LOG.debug("{} was created", ts))
                .collect(Collectors.toList());
    }

    private TrackSimilarity reduceTrackSimilarity(Fingerprint f, long track2, Stream<Long> data){
        return data.reduce(
                (TrackSimilarity) ImmutableTrackSimilarity.builder()
                        .track1(f.getTrackId())
                        .track2(track2)
                        .fingerprintType(f.getType())
                        .build(),
                (ts, th) -> ts.add(th.intValue()),
                TrackSimilarity::add
        );
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
