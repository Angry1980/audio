package angry1980.audio.model;

public class Peak {

    private long trackId;
    private final int time;
    private final long hash;

    public Peak(long trackId, int time, long hash) {
        this.trackId = trackId;
        this.time = time;
        this.hash = hash;
    }

    public long getTrackId() {
        return trackId;
    }

    public int getTime() {
        return time;
    }

    public long getHash() {
        return hash;
    }

    @Override
    public String toString() {
        return "Peak{" +
                "time=" + time +
                ", trackId=" + trackId +
                ", hash=" + hash +
                '}';
    }
}

