package angry1980.audio.model;

import java.util.Objects;

public class Track {

    private final long id;
    private final String path;
    //for test purpose only
    private final long cluster;

    public Track(long id, String path) {
        this(id, path, -1);
    }

    public Track(long id, String path, long cluster) {
        this.id = id;
        this.path = path;
        this.cluster = cluster;
    }

    public String getPath() {
        return path;
    }

    public long getId() {
        return id;
    }

    public long getCluster() {
        return cluster;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Track track = (Track) o;
        return id == track.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Track{" +
                "id=" + id +
                ", path='" + path + '\'' +
                ", cluster='" + cluster + '\'' +
                '}';
    }
}
