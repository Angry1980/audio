package angry1980.audio.config;

import angry1980.audio.Adapter;
import angry1980.audio.LocalAdapter;
import angry1980.audio.dao.TrackDAO;
import angry1980.audio.dao.TrackDAOFileImpl;
import angry1980.audio.dao.TrackSimilarityDAO;
import angry1980.audio.dao.TrackSimilarityDaoInMemoryImpl;
import angry1980.utils.FileUtils;
import angry1980.utils.Numbered;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Configuration
@PropertySource({"classpath:common.properties"})
public class CommonConfig {

    @Autowired
    private Environment env;

    @Bean
    public TrackSimilarityDAO trackSimilarityDAO(){
        return new TrackSimilarityDaoInMemoryImpl();
    }

    @Bean
    public Adapter adapter(){
        return new LocalAdapter();
    }

    @Bean
    public TrackDAO trackDAO(){
        Map<Long, Map<Long, Path>> files = new HashMap<>();
        Path dir = Paths.get(env.getProperty("music.input.folder"));
        List<Path> clusters = FileUtils.getDirs(dir);
        long fileId =0;
        for(long i = 0; i < clusters.size(); i++){
            Map<Long, Path> t = new HashMap<>();
            for(Path path : FileUtils.getFiles(clusters.get((int) i), ".mp3")){
                t.put(fileId, path);
                fileId++;
            }
            files.put(i, t);
        }
        return new TrackDAOFileImpl(files);
    }
}
