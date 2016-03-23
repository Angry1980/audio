package angry1980.audio.kafka;

import angry1980.audio.model.TrackSimilarity;

public class TrackSimilaritySerializer extends JsonSerializer<TrackSimilarity> {

    public TrackSimilaritySerializer() {
        super(TrackSimilarity.class);
    }
}
