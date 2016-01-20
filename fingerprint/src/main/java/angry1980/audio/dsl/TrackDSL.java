package angry1980.audio.dsl;

import angry1980.audio.model.FingerprintType;
import angry1980.audio.model.TrackSimilarity;

import java.util.List;
import java.util.Optional;

public interface TrackDSL {

    TrackBuilder track(long track);

    long[] tracks();

    SimilarityBuilder similarity(TrackSimilarity ts);


    interface Builder<T extends Builder<T>>{

    }

    interface TrackBuilder<T extends TrackBuilder<T>> extends Builder<T>{

        T is(long cluster);

        List<TrackSimilarity> getSimilarities();

        long getCluster();

    }

    interface SimilarityBuilder<T extends SimilarityBuilder<T>> extends Builder<T>{

        T typeOf(FingerprintType type);

        T addTrack(long trackId);

    }

}
