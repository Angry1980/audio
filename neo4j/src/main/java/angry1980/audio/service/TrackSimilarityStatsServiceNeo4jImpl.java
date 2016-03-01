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

import java.util.*;
import java.util.stream.Collectors;

public class TrackSimilarityStatsServiceNeo4jImpl implements TrackSimilarityStatsService{

    private GraphDatabaseService graphDB;
    private Template template;

    public TrackSimilarityStatsServiceNeo4jImpl(GraphDatabaseService graphDB) {
        this.graphDB = Objects.requireNonNull(graphDB);
        this.template = new Template(graphDB);
    }

    @Override
    public Observable<Stats> compareFingerprintTypes(Map<FingerprintType, Integer> minWeights) {
        int trackCount = getNodesCount(Neo4jNodeType.TRACK);
        return Observable.create(subscriber -> {
            Collection<FingerprintType> types = minWeights.keySet();
            types.stream().forEach(type ->
                    subscriber.onNext(stats(getResultDependsOnFingerprintType(minWeights, type), trackCount))
            );
            TypePair.pairs(types).stream().forEach(pair ->
                    subscriber.onNext(stats(compareFingerprintTypes(minWeights, pair.type1, pair.type2), trackCount))
            );
            subscriber.onNext(stats(getCommonCount(minWeights), trackCount));
            subscriber.onCompleted();
        });
    }

    private static class TypePair{

        static Collection<TypePair> pairs(Collection<FingerprintType> types){
            Collection<TypePair> pairs = new HashSet<>();
            types.stream().forEach(type1 ->
                    types.stream().filter(type2 -> !type1.equals(type2)).forEach(type2 ->
                            pairs.add(new TypePair(type1, type2))
                    )
            );
            return pairs;
        }

        final FingerprintType type1;
        final FingerprintType type2;

        private TypePair(FingerprintType type1, FingerprintType type2) {
            this.type1 = type1;
            this.type2 = type2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TypePair typePair = (TypePair) o;
            return (type1 == typePair.type1  && type2 == typePair.type2)
                    || (type1 == typePair.type2  && type2 == typePair.type1)
                    ;
        }

        @Override
        public int hashCode() {
            return Objects.hash(type1, type2) + Objects.hash(type2, type1);
        }
    }

    private Stats stats(Stats c, int trackCount){
        return ImmutableStats.builder()
                .from(c)
                .falseNegative(c.getFalseNegative().orElse(trackCount - c.getTruePositive()))
                .build();
    }

    private Stats getCommonCount(Map<FingerprintType, Integer> minWeights) {
        return template.execute(graphDB -> {
            return template.handle(new FingerprintTypeComparingAllQuery(minWeights)).getResult();
        });
    }

    private Stats compareFingerprintTypes(Map<FingerprintType, Integer> minWeights, FingerprintType type1, FingerprintType type2){
        return template.execute(graphDB -> {
            return template.handle(new FingerprintTypeComparingQuery(minWeights, type1, type2)).getResult();
        });
    }

    private Stats getResultDependsOnFingerprintType(Map<FingerprintType, Integer> minWeights, FingerprintType type){
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
    public Map<Long, List<Long>> generateClusters(Map<FingerprintType, Integer> minWeights) {
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
