package angry1980.audio.neo4j;

import angry1980.audio.model.ComparingType;

public class FingerprintTypePositiveQuery extends FingerprintTypeQuery {

    private static final String QUERY = "match (cluster1)<-[:IS]-(track1)-[similar:SIMILAR]->(:TRACK)-[:IS]->(cluster2)"
            + " where similar.type={fingerprintType} and similar.weight > {minWeight}"
            //+ " return track1.id, track2.id, similar.weight"
            + " with cluster1=cluster2 as r, track1 as track"
            + " return count(distinct(track)) as result, r"
            ;



    public FingerprintTypePositiveQuery(ComparingType type) {
        super(type);
    }

    public FingerprintTypePositiveQuery(ComparingType type, int minWeight) {
        super(type, minWeight);
    }

    @Override
    public String getQuery() {
        return QUERY;
    }
}
