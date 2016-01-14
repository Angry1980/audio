package angry1980.audio.netflix;

import angry1980.audio.model.FingerprintType;
import angry1980.audio.model.TrackSimilarity;
import com.netflix.nfgraph.OrdinalIterator;
import com.netflix.nfgraph.build.NFBuildGraph;
import com.netflix.nfgraph.spec.NFGraphSpec;
import com.netflix.nfgraph.spec.NFNodeSpec;
import com.netflix.nfgraph.spec.NFPropertySpec;
import com.netflix.nfgraph.util.OrdinalMap;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;

public class Tracks {

    private File source;
    private NFBuildGraph graph;
    private OrdinalMap<Long> tracks;
    private OrdinalMap<Long> clusters;
    private OrdinalMap<String> similarities;
    private OrdinalMap<FingerprintType> types;

    public Tracks(File source) {
        this.source = source;
        this.graph = new NFBuildGraph(getSchema());
        this.tracks = new OrdinalMap<>();
        this.clusters = new OrdinalMap<>();
        this.similarities = new OrdinalMap<>();
        this.types = new OrdinalMap<>();

    }
    private NFGraphSpec getSchema(){
        return new NFGraphSpec(
                new NFNodeSpec(
                        "Track",
                        new NFPropertySpec("has", "Similarity", NFPropertySpec.MULTIPLE | NFPropertySpec.COMPACT),
                        new NFPropertySpec("is", "TrackCluster", NFPropertySpec.SINGLE | NFPropertySpec.COMPACT)
                ),
                new NFNodeSpec("TrackCluster"),
                new NFNodeSpec(
                        "Similarity",
                        new NFPropertySpec("typeOf", "SimilarityType", NFPropertySpec.SINGLE | NFPropertySpec.COMPACT)
                ),
                new NFNodeSpec("SimilarityType")
        );
    }

    public TrackNodeBuilder track(long track){
        return new TrackNodeBuilder(track);
    }

    public SimilarityNodeBuilder similarity(int similarityOrdinal){
        return new SimilarityNodeBuilder(similarityOrdinal);
    }

    public int getTrackNode(long trackId){
        return tracks.get(trackId);
    }

    public OrdinalIterator getTrackSimilarities(int trackNode){
        return graph.getConnectionIterator("Track", trackNode, "has");
    }

    public void save(){
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
            out.writeInt(clusters.size());
            for(long node: clusters){
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

    public int addSimilarity(TrackSimilarity ts){
        return similarities.add(ts.getTrack1() + "-" + ts.getTrack2() + "-" + ts.getValue());
    }

    public Optional<TrackSimilarity> fetchSimilarity(long track, int similarityNode){
        String s = null;
        if(similarityNode >= 0){
            s = similarities.get(similarityNode);
        }
        if(s == null){
            return Optional.empty();
        }
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
                    types.get(graph.getConnection("Similarity", similarityNode, "typeOf"))
            ));
        } catch(NumberFormatException e){
            return Optional.empty();
        }
    }

    public class NodeBuilder {

        private int ordinal;
        private String nodeType;
        private String property;
        private int to;

        public NodeBuilder(String nodeType, int ordinal) {
            this.nodeType = nodeType;
            this.ordinal = ordinal;
        }

        public void addConnection(){
            graph.addConnection(nodeType, ordinal, property, to);
        }

        protected NodeBuilder property(String property){
            this.property = property;
            return this;
        }

        protected NodeBuilder to(int to){
            this.to = to;
            return this;
        }

    }

    public class TrackNodeBuilder extends NodeBuilder {

        public TrackNodeBuilder(long trackId) {
            super("Track", tracks.add(trackId));
        }

        public TrackNodeBuilder hasSimilarity(int similarityNode){
            return (TrackNodeBuilder) property("has").to(similarityNode);
        }

        public TrackNodeBuilder is(long cluster){
            return (TrackNodeBuilder) property("is").to(clusters.add(cluster));
        }
    }

    public class SimilarityNodeBuilder extends NodeBuilder {


        public SimilarityNodeBuilder(int ordinal) {
            super("Similarity", ordinal);
        }

        public SimilarityNodeBuilder typeOf(FingerprintType type){
            return (SimilarityNodeBuilder) property("typeOf").to(types.add(type));
        }
    }
}
