package angry1980.audio.dao;

import angry1980.audio.model.*;
import com.netflix.nfgraph.OrdinalIterator;
import com.netflix.nfgraph.util.OrdinalMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class TrackSimilarityDAONetflixImpl extends Netflix<String> implements TrackSimilarityDAO {

    private static Logger LOG = LoggerFactory.getLogger(TrackSimilarityDAONetflixImpl.class);

    public TrackSimilarityDAONetflixImpl(NetflixData data) {
        super(data);
    }

    @Override
    public List<TrackSimilarity> tryToFindByTrackId(long trackId) {
        LOG.debug("Try to find similarities for track {}", trackId);
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
        LOG.debug("There are {} existed similarities for track {}", tss.size(), trackId);
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
        LOG.debug("Try to save {}", entity);
        String value = value(entity);
        addConnection(value, NetflixRelationType.TYPE_OF, data.getTypes().add(entity.getFingerprintType()));
        int ordinal = data.getSimilarities().get(value);
        data.getGraph().addConnection(
                NetflixNodeType.TRACK.name(),
                data.getTracks().get(entity.getTrack1()),
                NetflixRelationType.HAS.name(),
                ordinal
        );
        LOG.debug("Connection from {} to similarity node {} was added", entity.getTrack1(), value);
        data.getGraph().addConnection(
                NetflixNodeType.TRACK.name(),
                data.getTracks().get(entity.getTrack2()),
                NetflixRelationType.HAS.name(),
                ordinal
        );
        LOG.debug("Connection from {} to similarity node {} was added", entity.getTrack2(), value);
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
            return Optional.of(similarity(r, type));
        } catch(NumberFormatException e){
            return Optional.empty();
        }

    }

    private TrackSimilarity similarity(String[] r, FingerprintType type){
        return ImmutableTrackSimilarity.builder()
                .track1(Long.decode(r[0]))
                .track2(Long.decode(r[1]))
                .value(Integer.decode(r[2]))
                .fingerprintType(type)
                    .build();
    }
}
