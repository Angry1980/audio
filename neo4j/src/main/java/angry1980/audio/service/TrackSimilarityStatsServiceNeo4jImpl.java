package angry1980.audio.service;

import angry1980.audio.model.*;
import org.neo4j.graphdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class TrackSimilarityStatsServiceNeo4jImpl implements TrackSimilarityStatsService{

    private static Logger LOG = LoggerFactory.getLogger(TrackSimilarityStatsServiceNeo4jImpl.class);

    private static final String FALSE_NEGATIVE_QUERY = "match (track1:TRACK)-[:IS]->(cluster1)"
            + " where not (track1)-[:SIMILAR{type:{fingerprintType}}]->(:TRACK)-[:IS]->(:CLUSTER{id:cluster1.id})"
            //+ " return track1.id"
            + " return count(DISTINCT(track1.id)) as result, false as r"

            ;

    private static final String POSITIVE_QUERY = "match (cluster1)<-[:IS]-(track1)-[similar:SIMILAR]->(track2)-[:IS]->(cluster2)"
            + " where similar.type={fingerprintType}"
            //+ " return track1.id, track2.id, similar.weight"
            + " with cluster1.id=cluster2.id as r"
            + " return count(r)/2 as result, r"
            ;

    private static final String COUNT_QUERY = "match (node)"
            + " where {nodeType} in labels(node)"
            + " return count(node) as result"
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
        try(Transaction tx = graphDB.beginTx()){
            Map<Boolean, Integer> positive = getValues(POSITIVE_QUERY, params);
            FingerprintTypeStats stats = ImmutableFingerprintTypeStats.builder()
                        .type(type)
                        .clustersCount(getNodesCount(Neo4jNodeType.CLUSTER))
                        .tracksCount(getNodesCount(Neo4jNodeType.TRACK))
                        .falseNegative(getValue(getValues(FALSE_NEGATIVE_QUERY, params), false))
                        .falsePositive(getValue(positive, false))
                        .truthPositive(getValue(positive, true))
                    .build();
            tx.success();
            return stats;
        }
    }

    private int getNodesCount(Neo4jNodeType type){
        Map<String, Object> params = new HashMap<>();
        params.put("nodeType", type.name());
        try(Result result = graphDB.execute(COUNT_QUERY, params)){
            return asStream(result)
                    .map(data -> data.getOrDefault("result", "0"))
                    .map(Object::toString)
                    .map(Integer::decode)
                    .findAny().orElse(0);
        }
    }

    private int getValue(Map<Boolean, Integer> values, boolean key){
        return values.getOrDefault(key, 0);
    }

    private Map<Boolean, Integer> getValues(String query, Map<String, Object> params){
        try(Result result = graphDB.execute(query, params)){
            return asStream(result).map(data -> new Record(
                            Boolean.parseBoolean(data.getOrDefault("r", "true").toString()),
                            Integer.decode(data.getOrDefault("result", "0").toString()))
                    ).collect(Collectors.toMap(Record::isTruth, Record::getValue));
        }
    }

    private Stream<Map<String, Object>> asStream(Result result){
        Iterable<Map<String, Object>> iterable = () -> result;
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    private class Record{
        private boolean truth;
        private int value;

        public Record(boolean truth, int value) {
            this.truth = truth;
            this.value = value;
        }

        public boolean isTruth() {
            return truth;
        }

        public int getValue() {
            return value;
        }
    }
}
