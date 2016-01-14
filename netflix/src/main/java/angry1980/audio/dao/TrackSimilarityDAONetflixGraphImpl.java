package angry1980.audio.dao;

import angry1980.audio.model.TrackSimilarity;
import angry1980.audio.netflix.Tracks;
import com.netflix.nfgraph.OrdinalIterator;
import java.util.*;

/*
 * There are not any mechanism for recreating an NFBuildGraph from an NFCompressedGraph.
 * So this implementation can be used for accumulating and saving results only.
 */
public class TrackSimilarityDAONetflixGraphImpl implements TrackSimilarityDAO {

    private Tracks tracks;

    public TrackSimilarityDAONetflixGraphImpl(Tracks tracks) {
        this.tracks = Objects.requireNonNull(tracks);
    }

    @Override
    public List<TrackSimilarity> tryToFindByTrackId(long trackId) {
        int tnode = tracks.getTrackNode(trackId);
        if(tnode < 0){
            return Collections.emptyList();
        }
        List<TrackSimilarity> tss = new ArrayList<>();
        OrdinalIterator iter = tracks.getTrackSimilarities(tnode);
        int s;
        while((s = iter.nextOrdinal()) != OrdinalIterator.NO_MORE_ORDINALS) {
            tracks.fetchSimilarity(trackId, s).ifPresent(tss::add);
        }
        return tss;
    }

    @Override
    public Optional<TrackSimilarity> create(TrackSimilarity trackSimilarity) {
        int snode = tracks.addSimilarity(trackSimilarity);
        tracks.similarity(snode).typeOf(trackSimilarity.getFingerprintType()).addConnection();
        tracks.track(trackSimilarity.getTrack1()).hasSimilarity(snode).addConnection();
        tracks.track(trackSimilarity.getTrack2()).hasSimilarity(snode).addConnection();
        return Optional.of(trackSimilarity);
    }

}
