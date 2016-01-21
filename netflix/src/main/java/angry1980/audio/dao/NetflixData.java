package angry1980.audio.dao;

import angry1980.audio.model.FingerprintType;
import angry1980.audio.model.NetflixNodeType;
import angry1980.audio.model.NetflixRelationType;
import com.netflix.nfgraph.build.NFBuildGraph;
import com.netflix.nfgraph.spec.NFGraphSpec;
import com.netflix.nfgraph.spec.NFNodeSpec;
import com.netflix.nfgraph.spec.NFPropertySpec;
import com.netflix.nfgraph.util.OrdinalMap;

public class NetflixData {

    private NFBuildGraph graph;
    private OrdinalMap<Long> tracks;
    private OrdinalMap<Long> clusters;
    private OrdinalMap<String> paths;
    private OrdinalMap<String> similarities;
    private OrdinalMap<FingerprintType> types;

    public NetflixData() {
        this.graph = new NFBuildGraph(getSchema());
        this.tracks = new OrdinalMap<>();
        this.clusters = new OrdinalMap<>();
        this.similarities = new OrdinalMap<>();
        this.types = new OrdinalMap<>();
        this.paths = new OrdinalMap<>();
    }

    private NFGraphSpec getSchema(){
        //todo: move to another place
        return new NFGraphSpec(
                new NFNodeSpec(
                        NetflixNodeType.TRACK.name(),
                        new NFPropertySpec(NetflixRelationType.HAS.name(), NetflixNodeType.SIMILARITY.name(), NFPropertySpec.MULTIPLE | NFPropertySpec.COMPACT),
                        new NFPropertySpec(NetflixRelationType.IS.name(), NetflixNodeType.TRACK_CLUSTER.name(), NFPropertySpec.SINGLE | NFPropertySpec.COMPACT),
                        new NFPropertySpec(NetflixRelationType.SITUATED.name(), NetflixNodeType.TRACK_PATH.name(), NFPropertySpec.SINGLE | NFPropertySpec.COMPACT)
                ),
                new NFNodeSpec(NetflixNodeType.TRACK_CLUSTER.name()),
                new NFNodeSpec(NetflixNodeType.TRACK_PATH.name()),
                new NFNodeSpec(
                        NetflixNodeType.SIMILARITY.name(),
                        new NFPropertySpec(NetflixRelationType.TYPE_OF.name(), NetflixNodeType.SIMILARITY_TYPE.name(), NFPropertySpec.SINGLE | NFPropertySpec.COMPACT)
                ),
                new NFNodeSpec(NetflixNodeType.SIMILARITY_TYPE.name())
        );
    }

    public OrdinalMap<String> getPaths() {
        return paths;
    }

    public OrdinalMap<Long> getTracks() {
        return tracks;
    }

    public OrdinalMap<Long> getClusters() {
        return clusters;
    }

    public OrdinalMap<String> getSimilarities() {
        return similarities;
    }

    public OrdinalMap<FingerprintType> getTypes() {
        return types;
    }

    public NFBuildGraph getGraph() {
        return graph;
    }

}
