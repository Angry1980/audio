package angry1980.audio.model;

import org.immutables.value.Value;

import java.util.Objects;

@Value.Immutable
public abstract class Track {

    public abstract String getPath();

    public abstract long getId();

    //for test purpose only
    @Value.Default
    public long getCluster() {
        return -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Track track = (Track) o;
        return getId() == track.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

}
