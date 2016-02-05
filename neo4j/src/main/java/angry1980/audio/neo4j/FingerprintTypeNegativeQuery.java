package angry1980.audio.neo4j;

import angry1980.audio.model.FingerprintType;

public class FingerprintTypeNegativeQuery extends FingerprintTypeQuery{

    private static final String QUERY = "match (track1)-[:IS]->(cluster1)"
            + " optional match (track1)-[similar:SIMILAR{type:{fingerprintType}}]->(track2)-[:IS]->(cluster1)"
            + " where (similar.weight >{minWeight})"
            + " return count(distinct(track1)) as result, not(similar is null) as r"
            ;



    public FingerprintTypeNegativeQuery(FingerprintType type, int minWeight) {
        super(type, minWeight);
    }

    @Override
    public String getQuery() {
        return QUERY;
    }
}
