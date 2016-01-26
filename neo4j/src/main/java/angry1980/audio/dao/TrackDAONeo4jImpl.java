package angry1980.audio.dao;

import angry1980.audio.model.ImmutableTrack;
import angry1980.audio.model.Neo4jNodeType;
import angry1980.audio.model.Neo4jRelationType;
import angry1980.audio.model.Track;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import java.util.Collection;

public class TrackDAONeo4jImpl extends Neo4jNode implements TrackDAO{

    public static final String PATH_PROPERTY_NAME = "path";

    public TrackDAONeo4jImpl(GraphDatabaseService graphDB) {
        super(graphDB);
    }

    @Override
    protected Neo4jNodeType getType() {
        return Neo4jNodeType.TRACK;
    }

    @Override
    public Collection<Track> findByCluster(long cluster) {
        return template(graphDB -> {
            return getConnectedEntities(graphDB, Neo4jNodeType.CLUSTER, cluster, Neo4jRelationType.IS, this::fromNodeToTrack);
        });
    }

    @Override
    public Track tryToGet(long id) {
        return template(graphDB -> {
            return getEntity(graphDB, id, this::fromNodeToTrack);
        });
    }

    @Override
    public Collection<Track> tryToGetAll() {
        return template(graphDB -> {
            return getAllEntities(graphDB, this::fromNodeToTrack);
        });
    }


    @Override
    public Track tryToCreate(Track entity) {
        template(graphDB -> {
            getOrCreateNode(graphDB, entity.getId(), createdNode -> {
                createdNode.setProperty(PATH_PROPERTY_NAME, entity.getPath());
                createdNode.createRelationshipTo(
                        getOrCreateNode(graphDB, Neo4jNodeType.CLUSTER, entity.getCluster()),
                        Neo4jRelationType.IS
                );
                return createdNode;
            });
        });
        return entity;
    }

    private Track fromNodeToTrack(Node node){
        return ImmutableTrack.builder()
                .id(getId(node))
                .path((String) node.getProperty(PATH_PROPERTY_NAME))
                //todo: cluster()
                .build();
    }
}
