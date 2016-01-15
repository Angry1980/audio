package angry1980.audio;

import angry1980.audio.dao.TrackDAO;
import angry1980.audio.model.Track;
import angry1980.utils.FileUtils;
import org.springframework.beans.factory.InitializingBean;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FileTracksProvider implements InitializingBean{

    private Path inputDir;
    private TrackDAO trackDAO;

    public FileTracksProvider(String inputDir, TrackDAO trackDAO) {
        this.inputDir = Paths.get(inputDir);
        this.trackDAO = Objects.requireNonNull(trackDAO);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<Long, Map<Long, Path>> files = new HashMap<>();
        List<Path> clusters = FileUtils.getDirs(inputDir);
        long fileId =0;
        for(long i = 0; i < clusters.size(); i++){
            Map<Long, Path> t = new HashMap<>();
            for(Path path : FileUtils.getFiles(clusters.get((int) i), ".mp3")){
                t.put(fileId, path);
                fileId++;
            }
            files.put(i, t);
        }
        files.entrySet().stream()
                .flatMap(entry -> entry.getValue().entrySet().stream()
                        .map(file -> new Track(file.getKey(), file.getValue().toString()))
                        .map(t -> t.setCluster(entry.getKey()))
                ).forEach(trackDAO::create)
        ;

    }
}
