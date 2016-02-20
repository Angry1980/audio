package angry1980.audio.stats;

import org.immutables.value.Value;

import java.util.List;
import java.util.OptionalInt;

@Value.Immutable
public abstract class Stats {

    public abstract int getFalsePositive();
    public abstract OptionalInt getFalseNegative();
    public abstract int getTruePositive();

    @Value.Derived
    public double getPrecision(){
        return getTruePositive() / (double)(getTruePositive() + getFalsePositive());
    }

    @Value.Derived
    public double getRecall(){
        return getTruePositive() / (double)(getTruePositive() + getFalseNegative().orElse(0));
    }

    @Value.Derived
    public double getF1(){
        double precision = getPrecision();
        double recall = getRecall();
        return 2 * precision * recall / (precision + recall);
    }

    public abstract List<FingerprintTypeData> getTypes();
}
