package angry1980.audio.similarity;

import angry1980.audio.model.ComparingType;
import angry1980.audio.model.FingerprintType;
import angry1980.audio.model.Track;
import angry1980.audio.model.TrackSimilarity;
import org.springframework.core.Ordered;
import rx.Observable;

import java.util.function.Predicate;

public interface FindSimilarTracks extends Predicate<FingerprintType>, Ordered{

    Observable<TrackSimilarity> apply(Track track, ComparingType type);

    @Override
    default boolean test(FingerprintType fingerprintType) {
        return true;
    }

    @Override
    default int getOrder() {
        return 0;
    }
}
