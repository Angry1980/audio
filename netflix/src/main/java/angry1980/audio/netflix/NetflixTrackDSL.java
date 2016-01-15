package angry1980.audio.netflix;

import angry1980.audio.dsl.TrackDSL;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/*
 * There are not any mechanism for recreating an NFBuildGraph from an NFCompressedGraph.
 * So this implementation can be used for accumulating and saving results only.
 */
public class NetflixTrackDSL implements TrackDSL {

    private File source;
    private NFBuildGraph graph;
    private OrdinalMap<Long> tracks;
    private OrdinalMap<Long> clusters;
    private OrdinalMap<String> similarities;
    private OrdinalMap<FingerprintType> types;

    public NetflixTrackDSL(File source) {
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

    @Override
    public TrackNetflixBuilder track(long track){
        return new TrackNetflixBuilder(track);
    }

    @Override
    public SimilarityNetflixBuilder similarity(TrackSimilarity ts){
        return new SimilarityNetflixBuilder(ts);
    }

    @Override
    public SimilarityNetflixBuilder similarity(int similarityNode){
        return new SimilarityNetflixBuilder(similarityNode);
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

    public abstract class NetflixBuilder<T extends NetflixBuilder<T>> implements Builder<T>{

        protected int ordinal;
        private String nodeType;
        private String property;
        private int to;

        public NetflixBuilder(String nodeType, int ordinal) {
            if(ordinal < 0){
                //todo: refactor
                throw new IllegalArgumentException();
            }
            this.nodeType = nodeType;
            this.ordinal = ordinal;
        }

        public T addConnection(){
            graph.addConnection(nodeType, ordinal, property, to);
            return back();
        }

        protected abstract T back();

        protected T property(String property){
            this.property = property;
            return back();
        }

        protected T to(int to){
            this.to = to;
            return back();
        }

    }

    public class TrackNetflixBuilder extends NetflixBuilder<TrackNetflixBuilder> implements TrackBuilder<TrackNetflixBuilder>{

        private long trackId;

        public TrackNetflixBuilder(long trackId) {
            super("Track", tracks.add(trackId));
            this.trackId = trackId;
        }

        @Override
        protected TrackNetflixBuilder back() {
            return this;
        }

        @Override
        public TrackNetflixBuilder hasSimilarity(int similarityNode){
            return property("has").to(similarityNode);
        }

        @Override
        public TrackNetflixBuilder is(long cluster){
            return property("is").to(clusters.add(cluster)).addConnection();
        }

        @Override
        public List<TrackSimilarity> getSimilarities(){
            List<TrackSimilarity> tss = new ArrayList<>();
            OrdinalIterator iter = graph.getConnectionIterator("Track", ordinal, "has");
            int s;
            while((s = iter.nextOrdinal()) != OrdinalIterator.NO_MORE_ORDINALS) {
                similarity(s).fetch(trackId).ifPresent(tss::add);
            }
            return tss;
        }
    }

    public class SimilarityNetflixBuilder extends NetflixBuilder<SimilarityNetflixBuilder> implements SimilarityBuilder<SimilarityNetflixBuilder>{

        public SimilarityNetflixBuilder(int ordinal) {
            super("Similarity", ordinal);
        }

        public SimilarityNetflixBuilder(String similarity){
            this(similarities.add(similarity));
        }

        @Override
        protected SimilarityNetflixBuilder back() {
            return this;
        }

        public SimilarityNetflixBuilder(TrackSimilarity ts){
            this(ts.getTrack1() + "-" + ts.getTrack2() + "-" + ts.getValue());
        }

        @Override
        public SimilarityNetflixBuilder typeOf(FingerprintType type){
            return property("typeOf").to(types.add(type)).addConnection();
        }

        @Override
        public SimilarityNetflixBuilder addTrack(long trackId){
            track(trackId).hasSimilarity(ordinal).addConnection();
            return this;
        }

        @Override
        public Optional<TrackSimilarity> fetch(long track){
            String s =  similarities.get(ordinal);
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
                        types.get(graph.getConnection("Similarity", ordinal, "typeOf"))
                ));
            } catch(NumberFormatException e){
                return Optional.empty();
            }
        }

    }
}
