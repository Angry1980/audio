package angry1980.audio.service;

import angry1980.audio.model.ComparingType;
import angry1980.audio.model.Track;
import angry1980.audio.model.TrackSimilarity;
import angry1980.audio.similarity.TrackSimilarities;
import rx.Observable;

public interface TrackSimilarityService {

    Observable<Track> getTracksToCalculateSimilarity();

    Observable<TrackSimilarities> findOrCalculateSimilarities(long trackId, ComparingType type, ComparingType... types);

    Observable<TrackSimilarities> findOrCalculateSimilarities(Track track, ComparingType type, ComparingType... types);

    Observable<TrackSimilarity> findSimilarities(ComparingType comparingType, boolean truthPositive);

    Observable<TrackSimilarity> findCommonSimilarities(ComparingType comparingType, boolean onlyTruthPositive);

    Observable<TrackSimilarity> findUniqueSimilarities(ComparingType comparingType, boolean onlyTruthPositive);

    Observable<TrackSimilarities> getReport();

}
