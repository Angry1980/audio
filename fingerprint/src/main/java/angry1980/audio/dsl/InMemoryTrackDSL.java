package angry1980.audio.dsl;

import angry1980.audio.model.FingerprintType;
import angry1980.audio.model.TrackSimilarity;

import java.util.*;

public class InMemoryTrackDSL implements TrackDSL {

    private Map<Long, List<TrackSimilarity>> similarities = new HashMap<>();

    @Override
    public TrackBuilder track(long track) {
        return new TrackBuilderImpl(track);
    }

    @Override
    public SimilarityBuilder similarity(TrackSimilarity ts) {
        return new SimilarityBuilderImpl(ts);
    }

    public class TrackBuilderImpl implements TrackBuilder<TrackBuilderImpl>{

        private long trackId;

        public TrackBuilderImpl(long trackId) {
            this.trackId = trackId;
        }

        @Override
        public TrackBuilderImpl is(long cluster) {
            return this;
        }

        @Override
        public List<TrackSimilarity> getSimilarities() {
            return similarities.get(trackId);
        }
    }

    public class SimilarityBuilderImpl implements SimilarityBuilder<SimilarityBuilderImpl>{

        private TrackSimilarity ts;

        public SimilarityBuilderImpl(TrackSimilarity ts) {
            this.ts = ts;
        }

        @Override
        public SimilarityBuilderImpl typeOf(FingerprintType type) {
            return this;
        }

        @Override
        public SimilarityBuilderImpl addTrack(long trackId) {
            similarities.computeIfAbsent(trackId, l -> new ArrayList<>()).add(ts.getTrack1() == trackId ? ts : ts.reverse());
            return this;
        }

    }
}
