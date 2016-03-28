package angry1980.audio.dao;

import angry1980.audio.model.ComparingType;
import angry1980.audio.model.TrackSimilarity;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.utils.UUIDs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;

public class TrackSimilarityDAOCassandraImpl implements TrackSimilarityDAO{

    /*

create table similarityCommand(
	id uuid primary key,
	track1 bigint,
	track2 bigint,
	value int,
	t int
);
     */
    private static Logger LOG = LoggerFactory.getLogger(TrackSimilarityDAOCassandraImpl.class);

    private Session session;
    private PreparedStatement insertStatement;

    public TrackSimilarityDAOCassandraImpl(Session session, String keyspace) {
        this.session = session;
        this.insertStatement = session.prepare(
                insertInto(keyspace, "similarityCommand")
                        .value("id", bindMarker())
                        .value("track1", bindMarker())
                        .value("track2", bindMarker())
                        .value("value", bindMarker())
                        .value("t", bindMarker())
        );
    }

    @Override
    public TrackSimilarity tryToCreate(TrackSimilarity entity) {
        try {
            session.execute(new BoundStatement(insertStatement).bind(
                    UUIDs.random(),
                    entity.getTrack1(),
                    entity.getTrack2(),
                    entity.getValue(),
                    entity.getComparingType().ordinal()
            ));
            return entity;
        } catch (Exception e) {
            LOG.error("Error while trying to save similarity:", e);
        }
        return null;
    }

    @Override
    public List<TrackSimilarity> tryToFindByTrackId(long trackId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<List<TrackSimilarity>> findTruthPositiveByFingerprintType(ComparingType type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<List<TrackSimilarity>> findFalsePositiveByFingerprintType(ComparingType type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<TrackSimilarity> tryToGetAll() {
        throw new UnsupportedOperationException();
    }

}
