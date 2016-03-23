package angry1980.audio.kafka;

import angry1980.audio.model.Track;

public class TrackSerializer extends JsonSerializer {

    public TrackSerializer() {
        super(Track.class);
    }
}
