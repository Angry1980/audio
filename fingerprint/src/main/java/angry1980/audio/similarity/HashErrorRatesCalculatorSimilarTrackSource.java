package angry1980.audio.similarity;

import angry1980.audio.model.ComparingType;
import angry1980.audio.model.TrackSimilarity;
import angry1980.audio.service.TrackSimilarityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

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
    public Optional<long[]> get(long sourceTrackId) {
        long[] result = similarityService.findOrCalculateSimilarities(sourceTrackId, comparingType)
                .toBlocking().first().getSimilarities().stream()
                            .filter(ts -> ts.getValue() > limit)
                            .mapToLong(TrackSimilarity::getTrack2)
                            .toArray();
        LOG.debug("There are {} tracks which are looking similar to {} by {}", new Object[]{result.length, sourceTrackId, comparingType});
        return Optional.of(result);
    }
}
