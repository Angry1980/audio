package angry1980.audio.stats;

import angry1980.audio.model.ComparingType;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public interface CalculateSimilaritiesWeights {

    static <T extends CalculateSimilaritiesWeights> void init(String[] args, Class<T> _class){
        SpringApplication sa = new SpringApplication(_class);
        sa.setAdditionalProfiles(
                ComparingType.CHROMAPRINT.name(),
                ComparingType.PEAKS.name(),
                ComparingType.LASTFM.name(),
                "NEO4J"
        );
        ConfigurableApplicationContext context = sa.run(args);
        context.registerShutdownHook();
        context.getBean(_class).calculate();
    }

    void calculate();
}
