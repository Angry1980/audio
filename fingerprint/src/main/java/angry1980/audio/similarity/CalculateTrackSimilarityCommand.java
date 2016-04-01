package angry1980.audio.similarity;

import angry1980.audio.model.ComparingType;
import angry1980.audio.model.Track;
import org.axonframework.commandhandling.annotation.TargetAggregateIdentifier;
import org.immutables.value.Value;

@Value.Immutable
public interface CalculateTrackSimilarityCommand {

    Track getTrack();

    ComparingType getType();

    @TargetAggregateIdentifier
    default long getTrackId(){
        return getTrack().getId();
    }

}
