package angry1980.audio.neo4j;

import angry1980.audio.model.FingerprintType;

public class FingerprintTypeFalseNegativeQuery extends FingerprintTypeQuery{

    private static final String QUERY = "match (track1:TRACK)-[:IS]->(cluster1)"
            + ", (track2:TRACK)-[:IS]->(:CLUSTER{id:cluster1.id})"
            + " where track1 <> track2"
            + " and (track1)-[:SIMILAR]->(track2)"
            + " and not (track1)-[:SIMILAR{type:{fingerprintType}}]->(track2)"
            + " return count(track1) as result, false as r"
            ;

    public FingerprintTypeFalseNegativeQuery(FingerprintType type) {
        super(type);
    }

    @Override
    public String getQuery() {
        return QUERY;
    }
}
