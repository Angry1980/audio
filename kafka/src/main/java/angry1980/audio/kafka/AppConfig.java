package angry1980.audio.kafka;

import angry1980.audio.Adapter;
import angry1980.audio.FileTracksProvider;
import angry1980.audio.LocalAdapter;
import angry1980.audio.dao.TrackDAO;
import angry1980.audio.similarity.TracksToCalculate;
import angry1980.audio.similarity.TracksToCalculateImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan(value = {"angry1980.audio.config"})
@PropertySource("classpath:kafka.properties")
public class AppConfig {

    @Autowired
    private TrackDAO trackDAO;
    @Value("${music.input.folder}")
    private String inputFolder;

    @Bean
    public FileTracksProvider tracksProvider(){
        return new FileTracksProvider(inputFolder, trackDAO);
    }

    @Bean
    public TracksToCalculate tracksToCalculate(){
        return new TracksToCalculateImpl(trackDAO);
    }

    @Bean
    public Adapter adapter(){
        return new LocalAdapter();
    }


}
