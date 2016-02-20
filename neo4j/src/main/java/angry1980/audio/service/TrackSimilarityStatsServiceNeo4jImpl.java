package angry1980.audio.service;

import angry1980.audio.model.*;
import angry1980.audio.neo4j.*;
import angry1980.audio.stats.*;
import angry1980.neo4j.NodeCountQuery;
import angry1980.neo4j.Template;
import angry1980.neo4j.louvain.Louvain;
import angry1980.neo4j.louvain.LouvainResult;
import org.neo4j.graphdb.*;
import rx.Observable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class TrackSimilarityStatsServiceNeo4jImpl implements TrackSimilarityStatsService{

    private GraphDatabaseService graphDB;
    private Template template;
    private Map<FingerprintType, Integer> minWeights;

    public TrackSimilarityStatsServiceNeo4jImpl(GraphDatabaseService graphDB, Map<FingerprintType, Integer> minWeights) {
        this.graphDB = Objects.requireNonNull(graphDB);
        this.template = new Template(graphDB);
        this.minWeights = Objects.requireNonNull(minWeights);
    }

    @Override
    public Observable<Stats> compareFingerprintTypes() {
        int trackCount = getNodesCount(Neo4jNodeType.TRACK);
        return Observable.create(subscriber -> {
            subscriber.onNext(stats(getResultDependsOnFingerprintType(FingerprintType.CHROMAPRINT), trackCount));
            subscriber.onNext(stats(getResultDependsOnFingerprintType(FingerprintType.LASTFM), trackCount));
            subscriber.onNext(stats(getResultDependsOnFingerprintType(FingerprintType.PEAKS), trackCount));
            subscriber.onNext(stats(compareFingerprintTypes(FingerprintType.CHROMAPRINT, FingerprintType.PEAKS), trackCount));
            subscriber.onNext(stats(compareFingerprintTypes(FingerprintType.CHROMAPRINT, FingerprintType.LASTFM), trackCount));
            subscriber.onNext(stats(compareFingerprintTypes(FingerprintType.LASTFM, FingerprintType.PEAKS), trackCount));
            subscriber.onNext(stats(getCommonCount(), trackCount));
            subscriber.onCompleted();
        });
    }

    private Stats stats(Stats c, int trackCount){
        return ImmutableStats.builder()
                .from(c)
                .falseNegative(c.getFalseNegative().orElse(trackCount - c.getTruePositive()))
                .build();
    }

    private Stats getCommonCount() {
        return template.execute(graphDB -> {
            return template.handle(new FingerprintTypeComparingAllQuery(minWeights)).getResult();
        });
    }

    private Stats compareFingerprintTypes(FingerprintType type1, FingerprintType type2){
        return template.execute(graphDB -> {
            return template.handle(new FingerprintTypeComparingQuery(minWeights, type1, type2)).getResult();
        });
    }

    private Stats getResultDependsOnFingerprintType(FingerprintType type){
        return getResultDependsOnFingerprintType(type, minWeights.get(type));
    }

    @Override
    public Stats getResultDependsOnFingerprintType(FingerprintType type, int minWeight) {
        return template.execute(graphDB -> {
            FingerprintTypeQuery positive = template.handle(new FingerprintTypePositiveQuery(type, minWeight));
            return ImmutableStats.builder()
                    .addTypes(ImmutableFingerprintTypeData.builder().type(type).weight(minWeight).build())
                    .falseNegative(template.handle(new FingerprintTypeNegativeQuery(type, minWeight)).getValue(false))
                    .falsePositive(positive.getValue(false))
                    .truePositive(positive.getValue(true))
                    .build();

        });

    }

    @Override
    public Map<Long, List<Long>> generateClusters() {
        Louvain louvain = new Louvain(graphDB, new LouvainTaskAdapter(minWeights));
        louvain.execute();
        LouvainResult result = louvain.getResult();
        return result.layer(0).getNode2CommunityMap().entrySet().stream()
                .collect(Collectors.groupingBy(Map.Entry::getValue, Collectors.mapping(Map.Entry::getKey, Collectors.toList())));
    }

    private int getNodesCount(Neo4jNodeType type){
        return template.handle(new NodeCountQuery(type.name())).getResult();
    }

}
