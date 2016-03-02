package angry1980.audio.stats;

import angry1980.audio.model.ComparingType;
import org.immutables.value.Value;

@Value.Immutable
public interface FingerprintTypeData {

    ComparingType getType();
    int getWeight();

}
