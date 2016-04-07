package angry1980.audio.similarity;

import angry1980.audio.model.ComparingType;
import angry1980.audio.model.Fingerprint;
import angry1980.audio.model.SimilarityType;
import angry1980.audio.model.TrackSimilarity;
import org.springframework.core.Ordered;
import rx.Observable;

import java.util.List;
import java.util.function.Predicate;

public interface Calculator<F extends Fingerprint>  extends Predicate<SimilarityType>, Ordered {

    Observable<TrackSimilarity> calculate(F fingerprint, ComparingType comparingType);

    @Override
    default int getOrder() {
        return 0;
    }

    @Override
    default boolean test(SimilarityType similarityType) {
        return true;
    }
}
