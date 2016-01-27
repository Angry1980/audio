package angry1980.neo4j;

import com.google.common.collect.ImmutableMap;
import org.neo4j.graphdb.Result;

import java.util.Map;
import java.util.Objects;

public class NodeCountQuery implements Query<NodeCountQuery> {

    private static final String QUERY = "match (node)"
            + " where {nodeType} in labels(node)"
            + " return count(node) as result"
            ;

    private final String nodeType;
    private int result;

    public NodeCountQuery(String nodeType) {
        this.nodeType = Objects.requireNonNull(nodeType);
    }

    @Override
    public String getQuery() {
        return QUERY;
    }

    @Override
    public Map<String, Object> getParams() {
        return ImmutableMap.of("nodeType", nodeType);

    }

    @Override
    public NodeCountQuery handle(Result result) {
        this.result = Query.asStream(result)
                .map(data -> data.getOrDefault("result", "0"))
                .map(Object::toString)
                .map(Integer::decode)
                .findAny()
                .orElse(0);
        return this;
    }

    public int getResult() {
        return result;
    }
}
