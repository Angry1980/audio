package angry1980.audio.similarity;

import angry1980.audio.model.TrackSimilarity;
import org.springframework.core.Ordered;

import java.util.Collections;
import java.util.List;

public class FindSimilarTracksFakeImpl implements FindSimilarTracks {

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public List<TrackSimilarity> apply(long value) {
        return Collections.emptyList();
    }
}
