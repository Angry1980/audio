package angry1980.audio.dsl;

import angry1980.audio.model.FingerprintType;
import angry1980.audio.model.TrackSimilarity;
import org.neo4j.graphdb.*;
import org.springframework.beans.factory.InitializingBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Neo4jTrackDSL implements TrackDSL, InitializingBean{

    private GraphDatabaseService graphDB;

    public Neo4jTrackDSL(GraphDatabaseService graphDB) {
        this.graphDB = Objects.requireNonNull(graphDB);
    }

    @Override
    public TrackBuilder track(long track) {
        return new Neo4jTrackBuilder(track);
    }

    @Override
    public long[] tracks() {
        try(Transaction tx = graphDB.beginTx()){
            List<Long> r = new ArrayList<>();
            graphDB.findNodes(NodeType.TRACK).forEachRemaining(
                    node -> r.add((Long) node.getProperty("id"))
            );
            tx.success();
            return r.stream().mapToLong(l -> l).toArray();
        }
    }

    @Override
    public SimilarityBuilder similarity(TrackSimilarity ts) {
        return new Neo4jSimilarityBuilder(ts);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //todo: create schema with unique constraints
    }

    public class Neo4jBuilder<T extends Neo4jBuilder<T>> implements Builder<T>{

        protected Node getOrCreate(NodeType type, long id){
            Node node = graphDB.findNode(type, "id", id);
            if(node != null){
                return node;
            }
            node = graphDB.createNode(type);
            node.setProperty("id", id);
            return node;
        }

    }

    public class Neo4jTrackBuilder extends Neo4jBuilder<Neo4jTrackBuilder> implements TrackBuilder<Neo4jTrackBuilder>{

        private long trackId;

        public Neo4jTrackBuilder(long trackId) {
            this.trackId = trackId;
        }

        @Override
        public Neo4jTrackBuilder is(long cluster) {
            try(Transaction tx = graphDB.beginTx()){
                getOrCreate(NodeType.TRACK, trackId)
                        .createRelationshipTo(getOrCreate(NodeType.CLUSTER, cluster),  RelsType.IS);
                tx.success();
            }
            return this;
        }

        @Override
        public List<TrackSimilarity> getSimilarities() {
            try(Transaction tx = graphDB.beginTx()){
                Node node = getOrCreate(NodeType.TRACK, trackId);
                List<TrackSimilarity> result = new ArrayList<>();
                for(Relationship r : node.getRelationships(RelsType.SIMILAR)){
                    result.add(
                            new TrackSimilarity(
                                    trackId,
                                    (Long)r.getEndNode().getProperty("id"),
                                    (Integer)r.getProperty("weight"),
                                    FingerprintType.valueOf((String) r.getProperty("type"))
                            )
                    );
                }
                tx.success();
                return result;
            }
        }

        @Override
        public long getCluster() {
            return 0;
        }
    }

    public class Neo4jSimilarityBuilder extends Neo4jBuilder<Neo4jSimilarityBuilder> implements SimilarityBuilder<Neo4jSimilarityBuilder>{

        private TrackSimilarity ts;

        public Neo4jSimilarityBuilder(TrackSimilarity ts) {
            this.ts = ts;
        }

        @Override
        public Neo4jSimilarityBuilder typeOf(FingerprintType type) {
            return this;
        }

        @Override
        public Neo4jSimilarityBuilder addTrack(long trackId) {
            try(Transaction tx = graphDB.beginTx()){
                Relationship r = getOrCreate(NodeType.TRACK, trackId)
                        .createRelationshipTo(getOrCreate(NodeType.TRACK, trackId == ts.getTrack1() ? ts.getTrack2() : ts.getTrack1()), RelsType.SIMILAR);
                r.setProperty("weight", ts.getValue());
                r.setProperty("type", ts.getFingerprintType().name());
                tx.success();
            }
            return this;
        }

    }

    private enum RelsType implements RelationshipType{
        IS,
        SIMILAR,
    }

    private enum NodeType implements Label{
        TRACK,
        CLUSTER,

    }
}
