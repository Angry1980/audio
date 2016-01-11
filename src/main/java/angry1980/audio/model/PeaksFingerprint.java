package angry1980.audio.model;

import java.util.Collections;
import java.util.List;

public class PeaksFingerprint implements Fingerprint {

    private final long trackId;
    private List<Peak> points;

    public PeaksFingerprint(long trackId) {
        this(trackId, Collections.emptyList());
    }

    public PeaksFingerprint(long trackId, List<Peak> points) {
        this.trackId = trackId;
        this.points = points;
    }

    @Override
    public long getTrackId() {
        return trackId;
    }

    @Override
    public FingerprintType getType() {
        return FingerprintType.PEAKS;
    }

    public List<Peak> getPoints() {
        return points;
    }

    public void setPoints(List<Peak> points) {
        if(points == null){
            return;
        }
        this.points = points;
    }

    @Override
    public String toString() {
        return "PeaksFingerprint{" +
                "trackId=" + trackId +
                ", type="+ getType() +
                '}';
    }

}
