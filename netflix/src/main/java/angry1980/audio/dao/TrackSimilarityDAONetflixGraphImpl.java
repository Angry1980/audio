package angry1980.audio.dao;

import angry1980.audio.model.FingerprintType;
import angry1980.audio.model.TrackSimilarity;
import com.netflix.nfgraph.OrdinalIterator;
import com.netflix.nfgraph.build.NFBuildGraph;
import com.netflix.nfgraph.spec.NFGraphSpec;
import com.netflix.nfgraph.spec.NFNodeSpec;
import com.netflix.nfgraph.spec.NFPropertySpec;
import com.netflix.nfgraph.util.OrdinalMap;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/*
 * There are not any mechanism for recreating an NFBuildGraph from an NFCompressedGraph.
 * So this implementation can be used for accumulating and saving results only.
 */
public class TrackSimilarityDAONetflixGraphImpl implements TrackSimilarityDAO {

    private File source;
    private NFBuildGraph graph;
    private OrdinalMap<Long> tracks;
    private OrdinalMap<String> similarities;
    private OrdinalMap<FingerprintType> types;

    public TrackSimilarityDAONetflixGraphImpl(File source) {
        this.source = Objects.requireNonNull(source);
    }

    @Override
    public List<TrackSimilarity> tryToFindByTrackId(long trackId) {
        int tnode = tracks.get(trackId);
        if(tnode < 0){
            return Collections.emptyList();
        }
        List<TrackSimilarity> tss = new ArrayList<>();
        OrdinalIterator iter = graph.getConnectionIterator("Track", tnode, "has");
        int s;
        while((s = iter.nextOrdinal()) != OrdinalIterator.NO_MORE_ORDINALS) {
            parse(trackId, similarities.get(s), types.get(graph.getConnection("Similarity", s, "typeOf")))
                .ifPresent(tss::add);
        }
        return tss;
    }

    @Override
    public Optional<List<TrackSimilarity>> findByTrackIdAndFingerprintType(long trackId, FingerprintType type) {
        return Optional.of(
                tryToFindByTrackId(trackId).stream()
                    .filter(ts -> ts.getFingerprintType().equals(type))
                    .collect(Collectors.toList())
        ).filter(list -> !list.isEmpty());
    }

    @Override
    public Optional<TrackSimilarity> create(TrackSimilarity trackSimilarity) {
        int snode = similarities.add(similarity(trackSimilarity));
        graph.addConnection("Similarity", snode, "typeOf", types.add(trackSimilarity.getFingerprintType()));
        graph.addConnection("Track", tracks.add(trackSimilarity.getTrack1()), "has", snode);
        graph.addConnection("Track", tracks.add(trackSimilarity.getTrack2()), "has", snode);
        return Optional.of(trackSimilarity);
    }

    public void shutdown() {
        DataOutputStream out = null;
        //todo: refactor
        try {
            if(source.exists()) {
                source.createNewFile();
            }
            out = new DataOutputStream(new FileOutputStream(source, false));
            out.writeInt(tracks.size());
            for(long node: tracks){
                out.writeLong(node);
            }
            out.writeInt(similarities.size());
            for(String node: similarities){
                out.writeUTF(node);
            }
            out.writeInt(types.size());
            for(FingerprintType node: types){
                out.writeUTF(node.name());
            }
            graph.compress().writeTo(out);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(out != null){
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void init(){
        NFGraphSpec schema = new NFGraphSpec(
                //todo: add cluster node
                new NFNodeSpec(
                        "Track",
                        new NFPropertySpec("has", "Similarity", NFPropertySpec.MULTIPLE | NFPropertySpec.COMPACT)
                ),
                new NFNodeSpec(
                        "Similarity",
                        new NFPropertySpec("typeOf", "SimilarityType", NFPropertySpec.SINGLE | NFPropertySpec.COMPACT)
                ),
                new NFNodeSpec("SimilarityType")
        );
        this.graph = new NFBuildGraph(schema);
        this.tracks = new OrdinalMap<>();
        this.similarities = new OrdinalMap<>();
        this.types = new OrdinalMap<>();
    }

    //todo: as separate class
    private String similarity(TrackSimilarity ts){
        return ts.getTrack1() + "-" + ts.getTrack2() + "-" + ts.getValue();
    }

    private Optional<TrackSimilarity> parse(long track, String s, FingerprintType type){
        String[] r = s.split("-");
        if(r.length != 3){
            return Optional.empty();
        }
        try{
            long track1 = Long.decode(r[0]);
            return Optional.of(new TrackSimilarity(
                    track == track1 ? track1 : Long.decode(r[1]),
                    track == track1 ? Long.decode(r[1]) : track1,
                    Integer.decode(r[2]),
                    type
            ));
        } catch(NumberFormatException e){
            return Optional.empty();
        }
    }
}
