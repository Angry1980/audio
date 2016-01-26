package angry1980.audio.service;

import angry1980.audio.model.*;
import angry1980.audio.neo4j.FingerprintTypeFalseNegativeQuery;
import angry1980.audio.neo4j.FingerprintTypePositiveQuery;
import angry1980.audio.neo4j.FingerprintTypeQuery;
import angry1980.audio.neo4j.QueryImpl;
import angry1980.audio.stats.FingerprintTypeComparing;
import angry1980.audio.stats.FingerprintTypeResult;
import angry1980.audio.stats.ImmutableFingerprintTypeComparing;
import angry1980.audio.stats.ImmutableFingerprintTypeResult;
import angry1980.neo4j.NodeCountQuery;
import angry1980.neo4j.QueryHandler;
import angry1980.neo4j.Template;
import com.google.common.collect.ImmutableMap;
import org.neo4j.graphdb.*;
import rx.Observable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TrackSimilarityStatsServiceNeo4jImpl implements TrackSimilarityStatsService{

    private QueryHandler queryHandler;
    private Template template;

    public TrackSimilarityStatsServiceNeo4jImpl(GraphDatabaseService graphDB) {
        this.queryHandler = new QueryHandler(graphDB);
        this.template = new Template(graphDB);
    }

    @Override
    public Observable<FingerprintTypeComparing> compareFingerprintTypes() {
        return Observable.create(subscriber -> {
            getCombinations().entrySet().stream()
                    .flatMap(entry -> entry.getValue().stream()
                                    .map(v -> this.compareFingerprintTypes(entry.getKey(), v))
                    )
                    .forEach(subscriber::onNext);
            subscriber.onCompleted();
        });
    }

    private Map<FingerprintType, List<FingerprintType>> getCombinations(){
        return ImmutableMap.of(FingerprintType.CHROMAPRINT, Arrays.asList(FingerprintType.PEAKS, FingerprintType.LASTFM),
                                FingerprintType.PEAKS, Arrays.asList(FingerprintType.LASTFM)
        );
    }

    private FingerprintTypeComparing compareFingerprintTypes(FingerprintType type1, FingerprintType type2){
        return template.execute(graphDB -> {
            queryHandler.execute(new QueryImpl(type1.name(), type2.name()));
            return ImmutableFingerprintTypeComparing.builder()
                    .type1(type1)
                    .type2(type2)
                    .minWeightInCommon1(0)
                    .minWeightInCommon2(0)
                    .common(0)
                    .all(0)
                    .build();
        });
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
