package angry1980.audio.neo4j;

import angry1980.audio.model.FingerprintType;

public class FingerprintTypePositiveQuery extends FingerprintTypeQuery {

    private static final String QUERY = "match (cluster1)<-[:IS]-(track1)-[similar:SIMILAR]->(track2)-[:IS]->(cluster2)"
            + " where similar.type={fingerprintType}"
            //+ " return track1.id, track2.id, similar.weight"
            + " with cluster1.id=cluster2.id as r"
            + " return count(r)/2 as result, r"
            ;

    public FingerprintTypePositiveQuery(FingerprintType type) {
        super(type);
    }

    @Override
    public String getQuery() {
        return QUERY;
    }
}
