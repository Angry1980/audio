package angry1980.audio.dao;

import angry1980.audio.model.Track;

import java.util.Optional;

public interface TrackDAO {

    Optional<Track> get(long  id);
}
