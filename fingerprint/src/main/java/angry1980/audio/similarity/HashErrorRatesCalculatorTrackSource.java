package angry1980.audio.similarity;

import angry1980.audio.model.Track;

import java.util.Collection;
import java.util.Optional;

public interface HashErrorRatesCalculatorTrackSource {

    Optional<Collection<Track>> get(long sourceTrackId);
}
