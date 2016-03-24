package angry1980.audio.stats;

import angry1980.audio.config.Neo4jConfig;
import com.google.common.collect.ImmutableMap;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public interface CalculateSimilaritiesWeights {

    static <T extends CalculateSimilaritiesWeights> void init(String[] args, Class<T> _class){
        SpringApplication sa = new SpringApplication(_class);
        //todo: as program argument
        sa.setDefaultProperties(ImmutableMap.of(
                Neo4jConfig.DATA_PATH_PROPERTY_NAME, "c:\\work\\ts.graphdb"
        ));
        ConfigurableApplicationContext context = sa.run(args);
        context.registerShutdownHook();
        context.getBean(_class).calculate();
    }

    void calculate();
}
