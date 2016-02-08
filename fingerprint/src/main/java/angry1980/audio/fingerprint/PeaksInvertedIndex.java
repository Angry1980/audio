package angry1980.audio.fingerprint;

import angry1980.audio.dao.PeakDAO;
import angry1980.audio.model.ImmutableTrackSimilarity;
import angry1980.audio.model.PeaksFingerprint;
import angry1980.audio.model.TrackSimilarity;
import angry1980.audio.similarity.Calculator;
import angry1980.utils.Numbered;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class PeaksInvertedIndex implements InvertedIndex<PeaksFingerprint>, Calculator<PeaksFingerprint> {

    private static Logger LOG = LoggerFactory.getLogger(PeaksInvertedIndex.class);

    private PeakDAO dataPointDAO;

    public PeaksInvertedIndex(PeakDAO dataPointDAO){
        this.dataPointDAO = Objects.requireNonNull(dataPointDAO);
    }

    @Override
    public List<TrackSimilarity> calculate(PeaksFingerprint fingerprint) {
        LOG.debug("Similarity calculation for {}", fingerprint);
        return fingerprint.getPoints().stream()
                //for each data point calculate time difference between points with the same hash
                .flatMap(dp1 -> dataPointDAO.findByHash(dp1.getHash()).stream()
                                    .filter(dp2 -> dp1.getTrackId() != dp2.getTrackId())
                                    .map(dp2 -> new Numbered<Integer>(dp2.getTrackId(), Math.abs(dp1.getTime() - dp2.getTime())))
                ).collect(
                    //for each track calculate count of same offsets
                    Collectors.groupingBy(Numbered::getNumber,
                            Collectors.groupingBy(Numbered::getValue, Collectors.counting())
                    )
                ).entrySet().stream()
                    //calculate sum of offsets counts for each track
                    .map(entry -> InvertedIndex.reduceTrackSimilarity(fingerprint, entry.getKey(),
                                        entry.getValue().entrySet().stream()
                                            .filter(entry1 -> entry1.getValue() > 10)
                                            .map(entry1 -> entry1.getValue())
                                        )
                    ).filter(ts -> ts.getValue() > 0)
                    .collect(Collectors.toList());
    }

    @Override
    public PeaksFingerprint save(PeaksFingerprint fingerprint) {
        if(fingerprint != null){
            LOG.debug("Creation of inverted index for {}", fingerprint);
            fingerprint.getPoints().stream().forEach(dataPointDAO::create);
        }
        return fingerprint;
    }

}
