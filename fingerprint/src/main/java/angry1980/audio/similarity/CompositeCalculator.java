package angry1980.audio.similarity;

import angry1980.audio.model.ComparingType;
import angry1980.audio.model.Fingerprint;
import angry1980.audio.model.TrackSimilarity;
import rx.Observable;

import java.util.List;
import java.util.Objects;

public class CompositeCalculator<F extends Fingerprint> implements Calculator<F> {

    private List<Calculator<F>> calculators;

    public CompositeCalculator(List<Calculator<F>> calculators) {
        this.calculators = Objects.requireNonNull(calculators);
    }

    @Override
    public Observable<TrackSimilarity> calculate(F fingerprint, ComparingType comparingType) {
        return calculators.stream()
                .filter(calc -> calc.test(comparingType.getSimilarityType()))
                .findAny()
                .map(calc -> calc.calculate(fingerprint, comparingType))
                .orElseGet(() -> Observable.empty());
    }
}
