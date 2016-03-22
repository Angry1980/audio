package angry1980.audio;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
@EnableAutoConfiguration
@ComponentScan(value = {"angry1980.audio.config"})
public class AppConfig {

    //todo: add logging setting

    @Bean
    public ScheduledExecutorService executor(){
        return Executors.newScheduledThreadPool(3);
    }

}
