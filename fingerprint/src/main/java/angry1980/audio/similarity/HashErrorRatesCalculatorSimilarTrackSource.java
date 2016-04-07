package angry1980.audio.similarity;

import angry1980.audio.model.ComparingType;
import angry1980.audio.model.TrackSimilarity;
import angry1980.audio.service.TrackSimilarityService;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.Objects;

public class HashErrorRatesCalculatorSimilarTrackSource implements HashErrorRatesCalculatorTrackSource {

    private static Logger LOG = LoggerFactory.getLogger(HashErrorRatesCalculatorSimilarTrackSource.class);

    private TrackSimilarityService similarityService;
    private int limit;
    private ComparingType comparingType;

    public HashErrorRatesCalculatorSimilarTrackSource(TrackSimilarityService similarityService, ComparingType comparingType) {
        this.similarityService = Objects.requireNonNull(similarityService);
        this.comparingType = Objects.requireNonNull(comparingType);
        this.limit = 1;
    }

    public HashErrorRatesCalculatorSimilarTrackSource setLimit(int limit) {
        this.limit = limit;
        return this;
    }

    @Override
    public Observable<Long> get(long sourceTrackId) {
        LongSet sent = new LongOpenHashSet();
        return similarityService.findOrCalculateSimilarities(sourceTrackId, comparingType)
                .filter(ts -> sent.contains(ts.getTrack2()))
                .doOnNext(ts -> sent.add(ts.getTrack2()))
                .map(TrackSimilarity::getTrack2);
    }
}
