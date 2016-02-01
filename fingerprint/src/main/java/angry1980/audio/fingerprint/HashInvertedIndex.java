package angry1980.audio.fingerprint;

import angry1980.audio.dao.TrackHashDAO;
import angry1980.audio.model.HashFingerprint;
import angry1980.audio.model.TrackHash;
import angry1980.audio.model.FingerprintType;
import angry1980.audio.model.TrackSimilarity;
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
                .mapToObj(hash -> new TrackHash(fingerprint.getTrackId(), hash))
                .forEach(hashDAO::create)
        ;
        return fingerprint;
    }

    @Override
    public List<TrackSimilarity> calculate(HashFingerprint fingerprint) {
        LOG.debug("Similarity calculation for", fingerprint);
        return Arrays.stream(fingerprint.getHashes())
                .mapToObj(hashDAO::findByHash)
                .flatMap(list -> list.stream())
                .filter(th -> fingerprint.getTrackId() != th.getTrackId())
                .collect(
                        Collectors.groupingBy(TrackHash::getTrackId)
                ).entrySet().stream()
                .map(entry -> entry.getValue().stream()
                    .reduce(
                        new TrackSimilarity(fingerprint.getTrackId(), entry.getKey(), 0, fingerprint.getType()),
                        (ts, th) -> ts.add(1),
                        TrackSimilarity::add
                    )
                )
                .filter(ts -> ts.getValue() > 0)
                .collect(Collectors.toList());
    }

}
