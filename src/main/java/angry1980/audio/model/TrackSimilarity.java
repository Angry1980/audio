package angry1980.audio.model;

import java.util.Objects;

public class TrackSimilarity {

    private final long track1;
    private final long track2;
    private final int value;
    private final FingerprintType fingerprintType;

    public TrackSimilarity(long track1, long track2, int value, FingerprintType fingerprintType) {
        this.track1 = track1;
        this.track2 = track2;
        this.value = value;
        this.fingerprintType = fingerprintType;
    }

    public long getTrack1() {
        return track1;
    }

    public long getTrack2() {
        return track2;
    }

    public int getValue() {
        return value;
    }

    public FingerprintType getFingerprintType() {
        return fingerprintType;
    }

    public TrackSimilarity add(int value){
        return new TrackSimilarity(this.track1, this.track2, this.value + value, this.fingerprintType);
    }

    public TrackSimilarity add(TrackSimilarity other){
        if(!this.equals(other)){
            return this;
        }
        return this.add(other.value);
    }

    public TrackSimilarity reverse(){
        return new TrackSimilarity(this.track2, this.track1, this.value, this.fingerprintType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrackSimilarity that = (TrackSimilarity) o;
        return track1 == that.track1 &&
                track2 == that.track2 &&
                fingerprintType == that.fingerprintType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(track1, track2, fingerprintType);
    }

    @Override
    public String toString() {
        return "TrackSimilarity{" +
                "track1=" + track1 +
                ", track2=" + track2 +
                ", value=" + value +
                ", fingerprintType=" + fingerprintType +
                '}';
    }
}
