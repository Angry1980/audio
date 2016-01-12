package angry1980.audio.model;

public class TrackHash {

    private final long trackId;
    private final int hash;

    public TrackHash(long trackId, int hash) {
        this.trackId = trackId;
        this.hash = hash;
    }

    public long getTrackId() {
        return trackId;
    }

    public int getHash() {
        return hash;
    }

    @Override
    public String toString() {
        return "TrackHash{" +
                "trackId=" + trackId +
                ", hash=" + hash +
                '}';
    }
}
