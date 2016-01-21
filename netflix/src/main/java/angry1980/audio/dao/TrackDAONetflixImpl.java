package angry1980.audio.dao;

import angry1980.audio.model.NetflixNodeType;
import angry1980.audio.model.NetflixRelationType;
import angry1980.audio.model.Track;
import com.netflix.nfgraph.util.OrdinalMap;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class TrackDAONetflixImpl extends Netflix<Long> implements TrackDAO{

    public TrackDAONetflixImpl(NetflixData data) {
        super(data);
    }

    @Override
    public Collection<Track> findByCluster(long cluster) {
        int ordinal = data.getClusters().get(cluster);
        return getAll().map(tracks -> tracks.stream()
                                .filter(track -> ordinal == getClusterNode(track.getId()))
                                .collect(Collectors.toList())
        ).orElseGet(() -> Collections.emptyList());
    }

    @Override
    public Track tryToGet(long id) {
        int ordinal = data.getTracks().get(id);
        if(ordinal < 0){
            return null;
        }
        return track(id);
    }

    @Override
    public Collection<Track> tryToGetAll() {
        return getAllNodes()
                .map(id -> this.track(id))
                .collect(Collectors.toList());
    }

    @Override
    public Track tryToCreate(Track entity) {
        addConnection(entity.getId(), NetflixRelationType.IS, data.getClusters().add(entity.getCluster()));
        addConnection(entity.getId(), NetflixRelationType.SITUATED, data.getPaths().add(entity.getPath()));
        return entity;
    }

    private Track track(long id){
        return new Track(id, trackPath(id)).setCluster(trackCluster(id));
    }

    private String trackPath(long id){
        return connectionValue(data.getPaths(), this::getPathNode, id);
    }

    private long trackCluster(long id){
        return connectionValue(data.getClusters(), this::getClusterNode, id);
    }

    private int getClusterNode(long trackId){
        return getConnectionNode(trackId, NetflixRelationType.IS);
    }

    private int getPathNode(long trackId){
        return getConnectionNode(trackId, NetflixRelationType.SITUATED);
    }

    @Override
    protected NetflixNodeType getNodeType() {
        return NetflixNodeType.TRACK;
    }

    @Override
    protected OrdinalMap<Long> getValues(NetflixData data) {
        return data.getTracks();
    }
}
