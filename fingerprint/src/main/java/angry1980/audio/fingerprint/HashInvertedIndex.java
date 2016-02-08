package angry1980.audio.fingerprint;

import angry1980.audio.dao.TrackHashDAO;
import angry1980.audio.model.*;
import angry1980.audio.similarity.Calculator;
import angry1980.utils.Numbered;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
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
/*
        return fingerprint.getHashes().stream()
                .map(hash -> hashDAO.findByHash(hash.getHash()))
                .flatMap(list -> list.stream())
                .filter(th -> fingerprint.getTrackId() != th.getTrackId())
                .collect(
                        Collectors.groupingBy(TrackHash::getTrackId)
                ).entrySet().stream()
                .map(entry -> InvertedIndex.reduceTrackSimilarity(fingerprint, entry.getKey(), entry.getValue().stream()))
                .filter(ts -> ts.getValue() > 0)
                .collect(Collectors.toList());
*/
        return fingerprint.getHashes().stream()
                //for each data point calculate time difference between points with the same hash
                .flatMap(dp1 -> hashDAO.findByHash(dp1.getHash()).stream()
                        .filter(dp2 -> dp1.getTrackId() != dp2.getTrackId())
                        .map(dp2 -> new Numbered<Integer>(dp2.getTrackId(), Math.abs(dp1.getTime() - dp2.getTime())))
                ).collect(
                        //for each track calculate count of same offsets
                        Collectors.groupingBy(
                                Numbered::getNumber,
                                Collectors.groupingBy(Numbered::getValue, Collectors.counting())
                        )
                ).entrySet().stream()
                //calculate sum of offsets counts for each track
                .map(entry -> InvertedIndex.reduceTrackSimilarity(fingerprint, entry.getKey(),
                        entry.getValue().entrySet().stream()
                                .filter(entry1 -> entry1.getValue() > filterWeight)
                                .map(Map.Entry::getValue)
                        )
                ).filter(ts -> ts.getValue() > minWeight)
                .collect(Collectors.toList());

    }

}
