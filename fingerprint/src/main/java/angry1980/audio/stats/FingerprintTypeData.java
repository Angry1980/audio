package angry1980.audio.stats;

import angry1980.audio.model.FingerprintType;
import org.immutables.value.Value;

@Value.Immutable
public interface FingerprintTypeData {

    FingerprintType getType();
    int getWeight();

}
