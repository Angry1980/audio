package angry1980.audio.kafka;

import angry1980.audio.model.Track;

public class TrackDeserializer extends JsonDeserializer {

    public TrackDeserializer() {
        super(Track.class);
    }
}
