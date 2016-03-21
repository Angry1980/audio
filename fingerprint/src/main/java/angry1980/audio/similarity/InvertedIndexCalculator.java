package angry1980.audio.similarity;

import angry1980.audio.fingerprint.InvertedIndex;
import angry1980.audio.model.*;
import angry1980.utils.Numbered;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class InvertedIndexCalculator implements Calculator<Fingerprint> {

    private static Logger LOG = LoggerFactory.getLogger(InvertedIndexCalculator.class);

    private double filterWeightPercent;
    private double minWeightPercent;
    private InvertedIndex<Fingerprint> index;


    public InvertedIndexCalculator(double filterWeightPercent, double minWeightPercent, InvertedIndex<Fingerprint> index) {
        this.filterWeightPercent = filterWeightPercent;
        this.minWeightPercent = minWeightPercent;
        this.index = Objects.requireNonNull(index);
    }

    @Override
    public boolean test(SimilarityType similarityType) {
        return SimilarityType.MASKED.equals(similarityType);
    }

    @Override
    public List<TrackSimilarity> calculate(Fingerprint fingerprint, ComparingType comparingType) {
        LOG.debug("Similarity calculation for {} of type {}", fingerprint.getTrackId(), fingerprint.getType());
        Long2ObjectMap<IntSortedSet> hashes = index.find(fingerprint);
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


}
