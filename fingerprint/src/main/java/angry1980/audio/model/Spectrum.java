package angry1980.audio.model;

import angry1980.audio.utils.Complex;
import org.immutables.value.Value;

import java.util.Objects;

@Value.Immutable
public interface Spectrum {

    long getTrackId();
    Complex[][] getData();
}
