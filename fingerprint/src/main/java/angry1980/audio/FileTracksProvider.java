package angry1980.audio;

import angry1980.audio.dao.TrackDAO;
import angry1980.audio.model.ImmutableTrack;
import angry1980.audio.track.ImmutableCreateTrackCommand;
import angry1980.utils.FileUtils;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FileTracksProvider {

    private static Logger LOG = LoggerFactory.getLogger(FileTracksProvider.class);

    private Path inputDir;
    private TrackDAO trackDAO;
    private CommandGateway commandGateway;

    public FileTracksProvider(String inputDir, TrackDAO trackDAO, CommandGateway commandGateway) {
        this.inputDir = Paths.get(inputDir);
        this.trackDAO = Objects.requireNonNull(trackDAO);
        this.commandGateway = Objects.requireNonNull(commandGateway);
    }

    public void init() throws Exception {
        getFiles().entrySet().stream()
                .flatMap(entry -> entry.getValue().entrySet().stream()
                        .filter(file -> !trackDAO.get(file.getKey()).isPresent())
                        .map(file -> ImmutableTrack.builder()
                                        .id(file.getKey())
                                        .path(file.getValue().toString())
                                        .cluster(entry.getKey())
                                        .build()
                        )
                ).forEach(track -> {
                    try {
                        commandGateway.send(ImmutableCreateTrackCommand.builder().track(track).build());
                    } catch(Exception e){
                        //todo: if conflict modification exception replay trackDAO
                        LOG.error("Error while trying to create track {}: {}", track, e);
                    }
                })
        ;
    }

    private Map<Long, Map<Long, Path>> getFiles(){
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
        return files;
    }

}
