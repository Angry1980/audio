package angry1980.audio.fingerprint;

import angry1980.audio.model.Fingerprint;
import angry1980.audio.model.ImmutableTrackSimilarity;
import angry1980.audio.model.TrackSimilarity;

import java.util.stream.Stream;

public interface InvertedIndex<F extends Fingerprint> {

    static TrackSimilarity reduceTrackSimilarity(Fingerprint f, long track2, Stream<Long> data){
        return data.reduce(
                (TrackSimilarity) ImmutableTrackSimilarity.builder()
                        .track1(f.getTrackId())
                        .track2(track2)
                        .fingerprintType(f.getType())
                        .build(),
                (ts, th) -> ts.add(th.intValue()),
                TrackSimilarity::add
        );
    }

    F save(F fingerprint);

}
