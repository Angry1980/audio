package angry1980.audio.service;

import angry1980.audio.model.*;
import org.neo4j.graphdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.HashMap;
import java.util.Map;

public class TrackSimilarityStatsServiceNeo4jImpl implements TrackSimilarityStatsService{

    private static Logger LOG = LoggerFactory.getLogger(TrackSimilarityStatsServiceNeo4jImpl.class);

    private static final String TRUTH_POSITIVE_QUERY = "match (cluster1)<-[:IS]-(track1)-[similar:SIMILAR]->(track2)-[:IS]->(cluster2)"
            + " where similar.type={fingerprintType}"
            + " and cluster1.id = cluster2.id "
            //+ " return track1.id, track2.id, similar.weight"
            + " return count(DISTINCT(similar.id)) as result"
            ;

    private static final String FALSE_POSITIVE_QUERY = "match (cluster1)<-[:IS]-(track1)-[similar:SIMILAR]->(track2)-[:IS]->(cluster2)"
            + " where similar.type={fingerprintType}"
            + " and cluster1.id <> cluster2.id "
            //+ " return track1.id, track2.id, similar.weight"
            + " return count(DISTINCT(similar.id)) as result"
            ;

    private static final String FALSE_NEGATIVE_QUERY = "match (track1:TRACK)-[:IS]->(cluster1)"
            + " where not (track1)-[:SIMILAR{type:{fingerprintType}}]->(:TRACK)-[:IS]->(:CLUSTER{id:cluster1.id})"
            //+ " return track1.id"
            + " return count(DISTINCT(track1.id)) as result"

            ;

    private GraphDatabaseService graphDB;

    public TrackSimilarityStatsServiceNeo4jImpl(GraphDatabaseService graphDB) {
        this.graphDB = graphDB;
    }

    @Override
    public Observable<FingerprintTypeStats> getFingerprintTypeStats() {
        return Observable.from(FingerprintType.values())
                            .map(this::getFingerprintTypeStats)
        ;
    }

    private FingerprintTypeStats getFingerprintTypeStats(FingerprintType type){
        Map<String, Object> params = new HashMap<>();
        params.put("fingerprintType", type.name());
        try(Transaction tx = graphDB.beginTx();
            Result result1 = graphDB.execute(TRUTH_POSITIVE_QUERY, params);
            Result result2 = graphDB.execute(FALSE_POSITIVE_QUERY, params);
            Result result3 = graphDB.execute(FALSE_NEGATIVE_QUERY, params)

        ){
            FingerprintTypeStats stats = ImmutableFingerprintTypeStats.builder()
                        .type(type)
                        //todo:implement
                        .clustersCount(0)
                        .tracksCount(0)
                        .falseNegative(getValue(result3))
                        .falsePositive(getValue(result2))
                        .truthPositive(getValue(result1))
                    .build();
            tx.success();
            return stats;
        }
    }

    private int getValue(Result result){
        if(!result.hasNext() ) {
            LOG.warn("Incorrect result");
            return 0;
        }
        Object value = result.next().get("result");
        if(value == null){
            LOG.warn("Incorrect result");
            return 0;
        }
        return Integer.decode(value.toString());
    }

}
