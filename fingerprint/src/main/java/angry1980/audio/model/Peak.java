package angry1980.audio.model;

import org.immutables.value.Value;

@Value.Immutable
public interface Peak {

    static Peak build(long trackId, int time, long hash){
        return ImmutablePeak.builder()
                .trackId(trackId)
                .time(time)
                .hash(hash)
                .build();
    }

    long getTrackId();
    int getTime();
    long getHash();

}

