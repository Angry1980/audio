package angry1980.audio.dao;

import angry1980.audio.model.FingerprintType;
import angry1980.audio.model.NetflixNodeType;
import angry1980.audio.model.NetflixRelationType;
import angry1980.audio.model.TrackSimilarity;
import com.netflix.nfgraph.OrdinalIterator;
import com.netflix.nfgraph.util.OrdinalMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class TrackSimilarityDAONetflixImpl extends Netflix<String> implements TrackSimilarityDAO {

    public TrackSimilarityDAONetflixImpl(NetflixData data) {
        super(data);
    }

    @Override
    public List<TrackSimilarity> tryToFindByTrackId(long trackId) {
        List<TrackSimilarity> tss = new ArrayList<>();
        OrdinalIterator it = data.getGraph().getConnectionIterator(NetflixNodeType.TRACK.name(), data.getTracks().get(trackId), NetflixRelationType.HAS.name());
        int s;
        while((s = it.nextOrdinal()) != OrdinalIterator.NO_MORE_ORDINALS) {
            String value =  data.getSimilarities().get(s);
            similarity(
                    value,
                    connectionValue(data.getTypes(), sv -> getConnectionNode(sv, NetflixRelationType.TYPE_OF), value)
            ).map(ts -> (ts.getTrack1() == trackId ? ts : ts.reverse()))
            .ifPresent(tss::add);
        }
        return tss;

    }

    @Override
    public Collection<TrackSimilarity> tryToGetAll() {
        Iterable<Long> iterable = () -> data.getTracks().iterator();
        return StreamSupport.stream(iterable.spliterator(), false)
                    .flatMap(t -> tryToFindByTrackId(t).stream())
                    .collect(Collectors.toSet());
    }

    @Override
    public TrackSimilarity tryToCreate(TrackSimilarity entity) {
        String value = value(entity);
        addConnection(value, NetflixRelationType.TYPE_OF, data.getTypes().add(entity.getFingerprintType()));
        int ordinal = data.getSimilarities().get(value);
        data.getGraph().addConnection(
                NetflixNodeType.TRACK.name(),
                data.getTracks().get(entity.getTrack1()),
                NetflixRelationType.HAS.name(),
                ordinal
        );
        data.getGraph().addConnection(
                NetflixNodeType.TRACK.name(),
                data.getTracks().get(entity.getTrack2()),
                NetflixRelationType.HAS.name(),
                ordinal
        );
        return entity;
    }

    @Override
    protected NetflixNodeType getNodeType() {
        return NetflixNodeType.SIMILARITY;
    }

    @Override
    protected OrdinalMap getValues(NetflixData data) {
        return data.getSimilarities();
    }

    private String value(TrackSimilarity ts){
        return ts.getTrack1() + "-" + ts.getTrack2() + "-" + ts.getValue();
    }

    private Optional<TrackSimilarity> similarity(String value, FingerprintType type){
        String[] r = value.split("-");
        if(r.length != 3){
            return Optional.empty();
        }
        try{
            return Optional.of(new TrackSimilarity(
                    Long.decode(r[0]),
                    Long.decode(r[1]),
                    Integer.decode(r[2]),
                    type
            ));
        } catch(NumberFormatException e){
            return Optional.empty();
        }

    }

}