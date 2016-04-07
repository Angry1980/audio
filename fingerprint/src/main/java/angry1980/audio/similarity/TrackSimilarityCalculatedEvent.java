package angry1980.audio.similarity;

import angry1980.audio.model.TrackSimilarity;
import org.immutables.value.Value;

@Value.Immutable
public interface TrackSimilarityCalculatedEvent {

    TrackSimilarity getSimilarity();

}
