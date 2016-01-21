package angry1980.audio;

import angry1980.audio.dao.TrackDAO;
import angry1980.audio.dao.TrackSimilarityDAO;

import java.util.Objects;

public class DataImporter {

    private TrackDataEnvironment from;

    public DataImporter(TrackDataEnvironment from) {
        this.from = from;
    }

    public void importTo(TrackDataEnvironment to){
        if(!to.isEmpty()){
            System.out.println("Data has been already imported");
            return;
        }
        from.getTrackDAO().getAll()
                .flatMap(to.getTrackDAO()::createAll)
                .ifPresent(
                        tracks -> tracks.stream()
                                    .forEach(track ->
                                            from.getTrackSimilarityDAO().findByTrackId(track.getId())
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

        public boolean isEmpty(){
            return !trackDAO.getAll().isPresent()
                    && !trackSimilarityDAO.getAll().isPresent();
        }

        public TrackDAO getTrackDAO() {
            return trackDAO;
        }

        public TrackSimilarityDAO getTrackSimilarityDAO() {
            return trackSimilarityDAO;
        }
    }
}
