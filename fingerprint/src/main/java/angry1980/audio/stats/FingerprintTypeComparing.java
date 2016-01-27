package angry1980.audio.stats;

import angry1980.audio.model.FingerprintType;
import org.immutables.value.Value;

@Value.Immutable
public interface FingerprintTypeComparing {

    FingerprintType getType1();
    FingerprintType getType2();
    int getCommon();
    int getAll();
    int getMinWeightInCommon1();
    int getMinWeightInCommon2();

}
