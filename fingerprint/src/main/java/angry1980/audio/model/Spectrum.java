package angry1980.audio.model;

import angry1980.audio.utils.Complex;

import java.util.Objects;

public class Spectrum {

    private final long trackId;
    private final Complex[][] data;

    public Spectrum(long trackId, Complex[][] results) {
        this.trackId = trackId;
        this.data = Objects.requireNonNull(results);
    }

    public long getTrackId() {
        return trackId;
    }

    public Complex[][] getData() {
        return data;
    }
}
