package angry1980.audio.service;

import angry1980.audio.model.*;
import angry1980.audio.neo4j.FingerprintTypeFalseNegativeQuery;
import angry1980.audio.neo4j.FingerprintTypePositiveQuery;
import angry1980.audio.neo4j.FingerprintTypeQuery;
import angry1980.audio.stats.FingerprintTypeComparing;
import angry1980.audio.stats.FingerprintTypeResult;
import angry1980.audio.stats.ImmutableFingerprintTypeResult;
import angry1980.neo4j.NodeCountQuery;
import angry1980.neo4j.QueryHandler;
import angry1980.neo4j.Template;
import org.neo4j.graphdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.Map;

public class TrackSimilarityStatsServiceNeo4jImpl implements TrackSimilarityStatsService{

    private static Logger LOG = LoggerFactory.getLogger(TrackSimilarityStatsServiceNeo4jImpl.class);


    private GraphDatabaseService graphDB;
    private QueryHandler queryHandler;
    private Template template;

    public TrackSimilarityStatsServiceNeo4jImpl(GraphDatabaseService graphDB) {
        this.graphDB = graphDB;
        this.queryHandler = new QueryHandler(graphDB);
        this.template = new Template(graphDB);
    }

    @Override
    public Observable<FingerprintTypeComparing> compareFingerprintTypes() {
        return null;
    }

    @Override
    public Observable<FingerprintTypeResult> getResultDependsOnFingerprintType() {
        return Observable.from(FingerprintType.values())
                            .map(this::getFingerprintTypeStats)
        ;
    }

    private FingerprintTypeResult getFingerprintTypeStats(FingerprintType type){
        return template.execute(graphDB -> {
            Map<Boolean, Integer> positive = getValues(new FingerprintTypePositiveQuery(type));
            return ImmutableFingerprintTypeResult.builder()
                    .type(type)
                    .clustersCount(getNodesCount(Neo4jNodeType.CLUSTER))
                    .tracksCount(getNodesCount(Neo4jNodeType.TRACK))
                    .falseNegative(getValue(getValues(new FingerprintTypeFalseNegativeQuery(type)), false))
                    .falsePositive(getValue(positive, false))
                    .truthPositive(getValue(positive, true))
                    .build();

        });
    }

    private int getNodesCount(Neo4jNodeType type){
        return queryHandler.execute(new NodeCountQuery(type.name()));
    }

    private Map<Boolean, Integer> getValues(FingerprintTypeQuery query){
        return queryHandler.execute(query);
    }

    private int getValue(Map<Boolean, Integer> values, boolean key){
        return values.getOrDefault(key, 0);
    }

}
