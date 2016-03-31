package angry1980.audio.service;

import angry1980.audio.model.ComparingType;
import angry1980.audio.model.Track;
import angry1980.audio.model.TrackSimilarity;
import rx.Observable;

public interface TrackSimilarityService {

    Observable<Track> getTracksToCalculateSimilarity();

    Observable<TrackSimilarity> findOrCalculateSimilarities(long trackId, ComparingType... types);

    Observable<TrackSimilarity> findOrCalculateSimilarities(Track track, ComparingType... types);

    Observable<TrackSimilarity> findSimilarities(ComparingType comparingType, boolean truthPositive);

    Observable<TrackSimilarity> findCommonSimilarities(ComparingType comparingType, boolean onlyTruthPositive);

    Observable<TrackSimilarity> findUniqueSimilarities(ComparingType comparingType, boolean onlyTruthPositive);

}
