package angry1980.audio;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
@EnableAutoConfiguration
@ComponentScan(value = {"angry1980.audio.config"})
public class AppConfig implements InitializingBean{

    //todo: add logging setting

    @Autowired
    private Optional<FileTracksProvider> tracksProvider;

    @Bean
    public ScheduledExecutorService executor(){
        return Executors.newScheduledThreadPool(3);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if(tracksProvider.isPresent()){
            tracksProvider.get().init();
        }
    }
}
