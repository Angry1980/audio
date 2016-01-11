package angry1980.audio.similarity;

import angry1980.audio.model.Fingerprint;
import angry1980.audio.model.TrackSimilarity;

import java.util.List;

public interface Calculator<F extends Fingerprint> {

    List<TrackSimilarity> calculate(F fingerprint);

}
