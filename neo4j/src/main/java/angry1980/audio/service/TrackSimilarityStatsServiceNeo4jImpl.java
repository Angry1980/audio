package angry1980.audio.service;

import angry1980.audio.model.*;
import angry1980.audio.neo4j.*;
import angry1980.audio.stats.FingerprintTypeComparing;
import angry1980.audio.stats.FingerprintTypeResult;
import angry1980.audio.stats.ImmutableFingerprintTypeResult;
import angry1980.neo4j.NodeCountQuery;
import angry1980.neo4j.Template;
import angry1980.neo4j.louvain.Louvain;
import angry1980.neo4j.louvain.LouvainResult;
import com.google.common.collect.ImmutableMap;
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

    public TrackSimilarityStatsServiceNeo4jImpl(GraphDatabaseService graphDB) {
        this.graphDB = Objects.requireNonNull(graphDB);
        this.template = new Template(graphDB);
        this.minWeights = ImmutableMap.of(FingerprintType.CHROMAPRINT, 20,
                FingerprintType.LASTFM, 400,
                FingerprintType.PEAKS, 1000
        );
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

    @Override
    public Map<Long, List<Long>> generateClusters() {
        Louvain louvain = new Louvain(graphDB, new LouvainTaskAdapter(minWeights));
        louvain.execute();
        LouvainResult result = louvain.getResult();
        return result.layer(0).getNode2CommunityMap().entrySet().stream()
                .collect(Collectors.groupingBy(Map.Entry::getValue, Collectors.mapping(Map.Entry::getKey, Collectors.toList())));
    }

    private FingerprintTypeResult getFingerprintTypeStats(FingerprintType type){
        return template.execute(graphDB -> {
            FingerprintTypeQuery positive = template.handle(new FingerprintTypePositiveQuery(type, minWeights.get(type)));
            return ImmutableFingerprintTypeResult.builder()
                    .type(type)
                    .clustersCount(getNodesCount(Neo4jNodeType.CLUSTER))
                    .tracksCount(getNodesCount(Neo4jNodeType.TRACK))
                    .falseNegative(template.handle(new FingerprintTypeFalseNegativeQuery(type)).getValue(false))
                    .falsePositive(positive.getValue(false))
                    .truthPositive(positive.getValue(true))
                    .uniqueSimilarityCount(template.handle(new UniqueSimilarityQuery(type, minWeights.get(type))).getResult())
                    .build();

        });
    }

    private int getNodesCount(Neo4jNodeType type){
        return template.handle(new NodeCountQuery(type.name())).getResult();
    }

}
