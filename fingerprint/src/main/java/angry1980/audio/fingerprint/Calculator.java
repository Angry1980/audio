package angry1980.audio.fingerprint;

import angry1980.audio.model.Fingerprint;
import angry1980.audio.model.Track;

import java.util.Optional;

public interface Calculator<F extends Fingerprint> {

    Optional<F> calculate(Track track);

}
