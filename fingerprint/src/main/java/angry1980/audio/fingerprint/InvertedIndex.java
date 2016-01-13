package angry1980.audio.fingerprint;

import angry1980.audio.model.Fingerprint;

public interface InvertedIndex<F extends Fingerprint> {

    F save(F fingerprint);

}
