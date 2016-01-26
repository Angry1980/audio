package angry1980.audio.neo4j;

import angry1980.audio.model.FingerprintType;

public class FingerprintTypeFalseNegativeQuery extends FingerprintTypeQuery{

    private static final String QUERY = "match (track1:TRACK)-[:IS]->(cluster1)"
            + " where not (track1)-[:SIMILAR{type:{fingerprintType}}]->(:TRACK)-[:IS]->(:CLUSTER{id:cluster1.id})"
            //+ " return track1.id"
            + " return count(DISTINCT(track1.id)) as result, false as r"
            ;

    public FingerprintTypeFalseNegativeQuery(FingerprintType type) {
        super(type);
    }

    @Override
    public String getQuery() {
        return QUERY;
    }
}
