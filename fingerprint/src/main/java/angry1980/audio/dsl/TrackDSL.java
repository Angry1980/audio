package angry1980.audio.dsl;

import angry1980.audio.model.FingerprintType;
import angry1980.audio.model.TrackSimilarity;

import java.util.List;
import java.util.Optional;

public interface TrackDSL {

    TrackBuilder track(long track);

    SimilarityBuilder similarity(TrackSimilarity ts);

    SimilarityBuilder similarity(int similarityNode);

    interface Builder<T extends Builder<T>>{

    }

    interface TrackBuilder<T extends TrackBuilder<T>> extends Builder<T>{

        T hasSimilarity(int similarityNode);

        T is(long cluster);

        List<TrackSimilarity> getSimilarities();

    }

    interface SimilarityBuilder<T extends SimilarityBuilder<T>> extends Builder<T>{

        T typeOf(FingerprintType type);

        T addTrack(long trackId);

        Optional<TrackSimilarity> fetch(long trackId);
    }

}
