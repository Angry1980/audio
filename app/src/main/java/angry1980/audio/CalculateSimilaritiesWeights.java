package angry1980.audio;

import angry1980.audio.model.FingerprintType;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public interface CalculateSimilaritiesWeights {

    static <T extends CalculateSimilaritiesWeights> void init(String[] args, Class<T> _class){
        SpringApplication sa = new SpringApplication(_class);
        sa.setAdditionalProfiles(
                FingerprintType.CHROMAPRINT.name(),
                FingerprintType.PEAKS.name(),
                FingerprintType.LASTFM.name(),
                "NEO4J"
        );
        ConfigurableApplicationContext context = sa.run(args);
        context.registerShutdownHook();
        context.getBean(_class).calculate();
    }

    void calculate();
}
