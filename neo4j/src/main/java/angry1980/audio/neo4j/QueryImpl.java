package angry1980.audio.neo4j;

import angry1980.neo4j.Query;
import com.google.common.collect.ImmutableMap;
import org.neo4j.graphdb.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class QueryImpl implements Query<String>{

    private static Logger LOG = LoggerFactory.getLogger(QueryImpl.class);

    private static final String QUERY = "match (cluster1)<-[:IS]-(track1:TRACK)-[similar1:SIMILAR{type:{type1}}]->(track2:TRACK)-[:IS]->(cluster1)"
            + " optional match (track1)-[similar2:SIMILAR{type:{type2}}]->(track2)"
            + " return similar1.id, similar2.id"
            //+ " return count(similar1.type), min(similar1.weight), min(similar2.weight)"
            ;

    private String type1;
    private String type2;

    public QueryImpl(String type1, String type2) {
        this.type1 = type1;
        this.type2 = type2;
    }

    @Override
    public String getQuery() {
        return QUERY;
    }

    @Override
    public Map<String, Object> getParams() {
        return ImmutableMap.of("type1", type1, "type2", type2);
    }

    @Override
    public String handle(Result result) {
        LOG.info(type1 + "-" + type2 + ":");
        LOG.info(result.resultAsString());
        return "";
    }
}
