package angry1980.audio.fingerprint;

import angry1980.audio.dao.TrackHashDAO;
import angry1980.audio.model.*;
import angry1980.audio.similarity.Calculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class HashInvertedIndex implements InvertedIndex<HashFingerprint>, Calculator<HashFingerprint> {

    private static Logger LOG = LoggerFactory.getLogger(HashInvertedIndex.class);

    private TrackHashDAO hashDAO;

    public HashInvertedIndex(TrackHashDAO hashDAO) {
        this.hashDAO = Objects.requireNonNull(hashDAO);
    }

    @Override
    public HashFingerprint save(HashFingerprint fingerprint) {
        LOG.debug("Creation of inverted index for {}", fingerprint);
        Arrays.stream(fingerprint.getHashes())
                .mapToObj(hash -> ImmutableTrackHash.builder().trackId(fingerprint.getTrackId()).hash(hash).build())
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
        LOG.debug("Similarity calculation for {}", fingerprint);
        return Arrays.stream(fingerprint.getHashes())
                .mapToObj(hashDAO::findByHash)
                .flatMap(list -> list.stream())
                .filter(th -> fingerprint.getTrackId() != th.getTrackId())
                .collect(
                        Collectors.groupingBy(TrackHash::getTrackId)
                ).entrySet().stream()
                .map(entry -> InvertedIndex.reduceTrackSimilarity(fingerprint, entry.getKey(), entry.getValue().stream()))
                .filter(ts -> ts.getValue() > 0)
                .collect(Collectors.toList());
    }

}
