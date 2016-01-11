package angry1980.audio.model;

public class ChromaprintFingerprint implements Fingerprint {

    private final long trackId;
    private final int[] hashes;

    public ChromaprintFingerprint(long trackId, int[] hashes) {
        this.trackId = trackId;
        this.hashes = hashes;
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
        return FingerprintType.CHROMAPRINT;
    }

    @Override
    public String toString() {
        return "ChromaprintFingerprint{" +
                "trackId=" + trackId +
                ", type="+ getType() +
                '}';
    }

}
