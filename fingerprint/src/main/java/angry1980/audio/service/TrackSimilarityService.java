package angry1980.audio.service;

import angry1980.audio.model.FingerprintType;
import angry1980.audio.model.Track;
import angry1980.audio.model.TrackSimilarity;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import rx.Observable;

import java.util.Collection;

public interface TrackSimilarityService {

    Observable<TrackSimilarities> findOrCalculateSimilarities(Track track);

    Observable<TrackSimilarities> findOrCalculateSimilarities(Track track, FingerprintType fingerprintType);

    class TrackSimilarities {
        private final Track track;
        private final ImmutableCollection<TrackSimilarity> similarities;

        public TrackSimilarities(Track track, Collection<TrackSimilarity> similarities) {
            this.track = track;
            this.similarities = ImmutableSet.copyOf(similarities);
        }

        public Track getTrack() {
            return track;
        }

        public Collection<TrackSimilarity> getSimilarities() {
            return similarities;
        }
    }
}
