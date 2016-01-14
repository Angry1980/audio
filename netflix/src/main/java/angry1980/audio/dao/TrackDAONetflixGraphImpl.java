package angry1980.audio.dao;

import angry1980.audio.model.Track;
import angry1980.audio.netflix.Tracks;

import java.util.Objects;
import java.util.Optional;

public class TrackDAONetflixGraphImpl extends TrackDAODecorator {

    private Tracks tracks;

    public TrackDAONetflixGraphImpl(Tracks tracks, TrackDAO prototype) {
        super(prototype);
        this.tracks = Objects.requireNonNull(tracks);
    }

    @Override
    public Optional<Track> create(Track track) {
        Optional<Track> result = super.create(track);
        if(result.isPresent()){
            tracks.track(result.get().getId()).is(result.get().getCluster()).addConnection();
        }
        return result;
    }
}
