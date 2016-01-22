package angry1980.audio.service;

import angry1980.audio.model.FingerprintType;
import angry1980.audio.model.Track;
import angry1980.audio.similarity.TrackSimilarities;
import rx.Observable;

public interface TrackSimilarityService {

    Observable<TrackSimilarities> findOrCalculateSimilarities(Track track);

    Observable<TrackSimilarities> findOrCalculateSimilarities(Track track, FingerprintType fingerprintType);

    Observable<TrackSimilarities> getReport();
}
