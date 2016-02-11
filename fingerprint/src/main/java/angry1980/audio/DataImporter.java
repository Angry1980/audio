package angry1980.audio;

import angry1980.audio.dao.TrackDAO;
import angry1980.audio.dao.TrackSimilarityDAO;
import angry1980.audio.model.FingerprintType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class DataImporter {

    private static Logger LOG = LoggerFactory.getLogger(DataImporter.class);

    private TrackDataEnvironment from;

    public DataImporter(TrackDataEnvironment from) {
        this.from = from;
    }

    public void importTo(TrackDataEnvironment to, FingerprintType type){
        if(!to.isEmpty(type)){
            LOG.info("Similarities for {} has been already imported", type);
            return;
        }
        from.getTrackDAO().getAll()
                .flatMap(to.getTrackDAO()::createAll)
                .ifPresent(
                        tracks -> tracks.stream()
                                    .forEach(track ->
                                            from.getTrackSimilarityDAO().findByTrackIdAndFingerprintType(track.getId(), type)
                                                .flatMap(to.getTrackSimilarityDAO()::createAll)
                                    )
                );
    }

    public static class TrackDataEnvironment{

        private TrackDAO trackDAO;
        private TrackSimilarityDAO trackSimilarityDAO;

        public TrackDataEnvironment(TrackDAO trackDAO, TrackSimilarityDAO trackSimilarityDAO) {
            this.trackDAO = Objects.requireNonNull(trackDAO);
            this.trackSimilarityDAO = Objects.requireNonNull(trackSimilarityDAO);
        }

        public boolean isEmpty(FingerprintType type){
            return trackSimilarityDAO.findByFingerprintType(type).map(c -> c.isEmpty()).orElse(true);
        }

        public TrackDAO getTrackDAO() {
            return trackDAO;
        }

        public TrackSimilarityDAO getTrackSimilarityDAO() {
            return trackSimilarityDAO;
        }
    }
}
