package angry1980.audio.service;

import angry1980.audio.model.*;
import angry1980.audio.neo4j.FingerprintTypeFalseNegativeQuery;
import angry1980.audio.neo4j.FingerprintTypePositiveQuery;
import angry1980.audio.neo4j.FingerprintTypeQuery;
import angry1980.audio.neo4j.FingerprintTypeComparingQuery;
import angry1980.audio.stats.FingerprintTypeComparing;
import angry1980.audio.stats.FingerprintTypeResult;
import angry1980.audio.stats.ImmutableFingerprintTypeResult;
import angry1980.neo4j.NodeCountQuery;
import angry1980.neo4j.Template;
import org.neo4j.graphdb.*;
import rx.Observable;

public class TrackSimilarityStatsServiceNeo4jImpl implements TrackSimilarityStatsService{

    private Template template;

    public TrackSimilarityStatsServiceNeo4jImpl(GraphDatabaseService graphDB) {
        this.template = new Template(graphDB);
    }

    @Override
    public Observable<FingerprintTypeComparing> compareFingerprintTypes() {
        return Observable.create(subscriber -> {
            subscriber.onNext(compareFingerprintTypes(FingerprintType.CHROMAPRINT, FingerprintType.PEAKS));
            subscriber.onNext(compareFingerprintTypes(FingerprintType.CHROMAPRINT, FingerprintType.LASTFM));
            subscriber.onNext(compareFingerprintTypes(FingerprintType.LASTFM, FingerprintType.PEAKS));
            subscriber.onCompleted();
        });
    }

    private FingerprintTypeComparing compareFingerprintTypes(FingerprintType type1, FingerprintType type2){
        return template.execute(graphDB -> {
            return template.handle(new FingerprintTypeComparingQuery(type1, type2))
                    .merge(template.handle(new FingerprintTypeComparingQuery(type2, type1)));
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
            FingerprintTypeQuery positive = template.handle(new FingerprintTypePositiveQuery(type));
            return ImmutableFingerprintTypeResult.builder()
                    .type(type)
                    .clustersCount(getNodesCount(Neo4jNodeType.CLUSTER))
                    .tracksCount(getNodesCount(Neo4jNodeType.TRACK))
                    .falseNegative(template.handle(new FingerprintTypeFalseNegativeQuery(type)).getValue(false))
                    .falsePositive(positive.getValue(false))
                    .truthPositive(positive.getValue(true))
                    .build();

        });
    }

    private int getNodesCount(Neo4jNodeType type){
        return template.handle(new NodeCountQuery(type.name())).getResult();
    }

}
