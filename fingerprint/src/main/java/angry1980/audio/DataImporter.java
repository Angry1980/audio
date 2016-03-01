package angry1980.audio;

import angry1980.audio.dao.TrackDAO;
import angry1980.audio.dao.TrackSimilarityDAO;
import angry1980.audio.model.ComparingType;
import angry1980.audio.model.ImmutableTrackSimilarity;
import angry1980.audio.model.Track;
import angry1980.audio.model.TrackSimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DataImporter {

    private static Logger LOG = LoggerFactory.getLogger(DataImporter.class);

    private TrackDataEnvironment from;

    public DataImporter(TrackDataEnvironment from) {
        this.from = from;
    }

    public void importTo(TrackDataEnvironment to, ComparingType type, ComparingType goal){
        if(!to.isEmpty(goal)){
            LOG.info("Similarities for {} has been already imported", goal);
            return;
        }
        Consumer<Collection<Track>> importer = tracks -> importTracks(tracks, to, type, goal);
        from.getTrackDAO().getAll()
                .flatMap(to.getTrackDAO()::createAll)
                .ifPresent(importer);
    }

    private void importTracks(Collection<Track> tracks, TrackDataEnvironment to, ComparingType type, ComparingType goal){
        Consumer<Track> importer = track -> importTrack(track, to, type, goal);
        int size = tracks.size();
        AtomicInteger counter = new AtomicInteger();
        tracks.stream()
                .peek(track -> LOG.debug("Similarities of {} from {} tracks was imported", counter.getAndIncrement(), size))
                .forEach(importer);
    }

    private void importTrack(Track track, TrackDataEnvironment to, ComparingType type, ComparingType goal){
        from.getTrackSimilarityDAO().findByTrackIdAndFingerprintType(track.getId(), type)
                .map(list -> transform(list, type, goal))
                .flatMap(to.getTrackSimilarityDAO()::createAll);
    }

    private List<TrackSimilarity> transform(List<TrackSimilarity> input, ComparingType type, ComparingType goal){
        if(type.equals(goal)){
            return input;
        }
        return input.stream()
                .map(ts -> ImmutableTrackSimilarity.builder().from(ts).comparingType(goal).build())
                .collect(Collectors.toList());
    }

    public static class TrackDataEnvironment{

        private TrackDAO trackDAO;
        private TrackSimilarityDAO trackSimilarityDAO;

        public TrackDataEnvironment(TrackDAO trackDAO, TrackSimilarityDAO trackSimilarityDAO) {
            this.trackDAO = Objects.requireNonNull(trackDAO);
            this.trackSimilarityDAO = Objects.requireNonNull(trackSimilarityDAO);
        }

        public boolean isEmpty(ComparingType type){
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
