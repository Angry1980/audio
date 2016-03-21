package angry1980.audio.fingerprint;

import angry1980.audio.model.Fingerprint;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

public interface InvertedIndex<F extends Fingerprint> {

    F save(F fingerprint);

    // track -> list of times with the same hash
    Long2ObjectMap<IntSortedSet> find(F fingerprint);

}
