package angry1980.audio.kafka;

import angry1980.audio.model.TrackSimilarity;

public class TrackSimilarityDeserializer extends JsonDeserializer<TrackSimilarity> {

    public TrackSimilarityDeserializer() {
        super(TrackSimilarity.class);
    }
}
