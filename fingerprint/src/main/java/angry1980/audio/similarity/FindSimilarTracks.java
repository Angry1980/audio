package angry1980.audio.similarity;

import angry1980.audio.model.ComparingType;
import angry1980.audio.model.TrackSimilarity;
import org.springframework.core.Ordered;

import java.util.List;
import java.util.function.Predicate;

public interface FindSimilarTracks extends Predicate<ComparingType>,
                                                Ordered{

    List<TrackSimilarity> apply(long track, ComparingType type);

    @Override
    default boolean test(ComparingType comparingType) {
        return true;
    }

    @Override
    default int getOrder() {
        return 0;
    }
}
