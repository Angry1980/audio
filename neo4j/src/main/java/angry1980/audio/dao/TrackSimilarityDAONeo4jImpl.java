package angry1980.audio.dao;

import angry1980.audio.model.FingerprintType;
import angry1980.audio.model.Neo4jNodeType;
import angry1980.audio.model.Neo4jRelationType;
import angry1980.audio.model.TrackSimilarity;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TrackSimilarityDAONeo4jImpl extends Neo4jRelation implements TrackSimilarityDAO {

    public TrackSimilarityDAONeo4jImpl(GraphDatabaseService graphDB) {
        super(graphDB);
    }

    @Override
    public List<TrackSimilarity> tryToFindByTrackId(long trackId) {
        return template(graphDB -> {
            //return getEntities(graphDB, Neo4jNodeType.TRACK, trackId, Neo4jRelationType.SIMILAR, this::fromRelationToTrackSimilarity);
            return getNode(graphDB, Neo4jNodeType.TRACK, trackId)
                    .map(node -> getConnections(node, Neo4jRelationType.SIMILAR, this::fromRelationToTrackSimilarity))
                    .orElseGet(() -> Collections.emptyList());
        });
    }

    @Override
    public Collection<TrackSimilarity> tryToGetAll() {
        return template(graphDB -> {
            return getAllNodes(graphDB, Neo4jNodeType.TRACK)
                    .map(this::getId)
                    .flatMap(nodeId -> tryToFindByTrackId(nodeId).stream())
                    .collect(Collectors.toSet());
        });
    }

    @Override
    public TrackSimilarity tryToCreate(TrackSimilarity entity) {
        template(graphDB -> {
            getNode(graphDB, Neo4jNodeType.TRACK, entity.getTrack1())
                .ifPresent(node1 -> {
                    getNode(graphDB, Neo4jNodeType.TRACK, entity.getTrack2())
                            .ifPresent(node2 -> {
                                getOrCreateRelation(node1, node2, entity);
                                getOrCreateRelation(node2, node1, entity);
                            });
                });
            ;
        });
        return entity;
    }

    private void getOrCreateRelation(Node from, Node to, TrackSimilarity s){
        Relationship r = getConnections(from, Neo4jRelationType.SIMILAR)
                .filter(rl -> rl.getEndNode().getId() == to.getId())
                .filter(rl -> s.getFingerprintType().name().equals(rl.getProperty("type")))
                .findAny()
                .orElseGet(() -> from.createRelationshipTo(to, Neo4jRelationType.SIMILAR));
        r.setProperty("weight", s.getValue());
        r.setProperty("type", s.getFingerprintType().name());
    }

    private TrackSimilarity fromRelationToTrackSimilarity(Relationship r){
        return new TrackSimilarity(
                getId(r.getStartNode()),
                getId(r.getEndNode()),
                (Integer)r.getProperty("weight"),
                FingerprintType.valueOf((String) r.getProperty("type"))
        );
    }
}
