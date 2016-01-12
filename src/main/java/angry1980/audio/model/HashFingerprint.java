package angry1980.audio.model;

public class HashFingerprint implements Fingerprint {

    private final long trackId;
    private final int[] hashes;
    private final FingerprintType fingerprintType;

    public HashFingerprint(long trackId, int[] hashes, FingerprintType fingerprintType) {
        this.trackId = trackId;
        this.hashes = hashes;
        this.fingerprintType = fingerprintType;
    }

    @Override
    public long getTrackId() {
        return trackId;
    }

    public int[] getHashes() {
        return hashes;
    }

    @Override
    public FingerprintType getType() {
        return fingerprintType;
    }

    @Override
    public String toString() {
        return "HashFingerprint{" +
                "trackId=" + trackId +
                ", type="+ fingerprintType +
                '}';
    }

}
