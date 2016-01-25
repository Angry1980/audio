package angry1980.audio.fingerprint;

import angry1980.audio.dao.TrackHashDAO;
import angry1980.audio.model.*;
import angry1980.audio.similarity.Calculator;

import java.util.*;
import java.util.stream.Collectors;

public class HashInvertedIndex implements InvertedIndex<HashFingerprint>, Calculator<HashFingerprint> {

    private TrackHashDAO hashDAO;

    public HashInvertedIndex(TrackHashDAO hashDAO) {
        this.hashDAO = Objects.requireNonNull(hashDAO);
    }

    @Override
    public HashFingerprint save(HashFingerprint fingerprint) {
        Arrays.stream(fingerprint.getHashes())
                .mapToObj(hash -> ImmutableTrackHash.builder().trackId(fingerprint.getTrackId()).hash(hash).build())
                .forEach(hashDAO::create)
        ;
        return fingerprint;
    }

    @Override
    public List<TrackSimilarity> calculate(HashFingerprint fingerprint) {
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
