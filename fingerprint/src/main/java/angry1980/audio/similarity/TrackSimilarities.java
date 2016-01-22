package angry1980.audio.similarity;

import angry1980.audio.model.Track;
import angry1980.audio.model.TrackSimilarity;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TrackSimilarities {

    private final Track track;
    private final ImmutableCollection<TrackSimilarity> similarities;

    public TrackSimilarities(Track track, Collection<TrackSimilarity> similarities) {
        this.track = track;
        this.similarities = ImmutableSet.copyOf(similarities);
    }

    public Track getTrack() {
        return track;
    }

    public Collection<TrackSimilarity> getSimilarities() {
        return similarities;
    }

    public Map<Long, List<TrackSimilarity>> groupByTrack(){
        return similarities.stream()
                .collect(Collectors.groupingBy(ts -> ts.getTrack2()));
    }
}
