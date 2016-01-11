package angry1980.audio.config;

import angry1980.audio.Adapter;
import angry1980.audio.LocalAdapter;
import angry1980.audio.dao.TrackDAO;
import angry1980.audio.dao.TrackDAOFileImpl;
import angry1980.audio.dao.TrackSimilarityDAO;
import angry1980.audio.dao.TrackSimilarityDaoInMemoryImpl;
import angry1980.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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
        List<Path> files = FileUtils.getFiles(Paths.get(env.getProperty("music.input.folder")), ".mp3");
        return new TrackDAOFileImpl(files);
    }
}
