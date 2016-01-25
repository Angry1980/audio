package angry1980.audio.model;

import angry1980.audio.utils.Complex;
import org.immutables.value.Value;

import java.util.Objects;

@Value.Immutable
public interface Spectrum {

    static Spectrum build(long trackId, Complex[][] data){
        return ImmutableSpectrum.builder()
                .trackId(trackId)
                .data(data)
                .build();
    }

    long getTrackId();
    Complex[][] getData();
}
