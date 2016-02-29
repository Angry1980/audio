package angry1980.audio.similarity;

import java.util.Optional;

public interface HashErrorRatesCalculatorTrackSource {

    Optional<long[]> get(long sourceTrackId);
}
