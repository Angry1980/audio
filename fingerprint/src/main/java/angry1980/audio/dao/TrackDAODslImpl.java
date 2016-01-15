package angry1980.audio.dao;

import angry1980.audio.dsl.TrackDSL;
import angry1980.audio.model.Track;


import java.util.Objects;
import java.util.Optional;

public class TrackDAODslImpl extends TrackDAODecorator {

    private TrackDSL trackDSL;

    public TrackDAODslImpl(TrackDSL trackDSL, TrackDAO prototype) {
        super(prototype);
        this.trackDSL = Objects.requireNonNull(trackDSL);
    }

    @Override
    public Optional<Track> create(Track track) {
        Optional<Track> result = super.create(track);
        if(result.isPresent()){
            trackDSL.track(result.get().getId()).is(result.get().getCluster());
        }
        return result;
    }
}
