package angry1980.audio.stats;

import angry1980.audio.AppConfig;
import angry1980.audio.neo4j.Neo4jDAOConfig;
import angry1980.audio.model.ComparingType;
import angry1980.audio.service.TrackSimilarityStatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Arrays;

@Configuration
@Import(value = {AppConfig.class, Neo4jDAOConfig.class})
public class CalculateBestFpTpWeights {

    private static Logger LOG = LoggerFactory.getLogger(CalculateBestFpTpWeights.class);

    @Autowired
    private TrackSimilarityStatsService trackSimilarityStatsService;

    public static void main(String[] args){
        SpringApplication sa = new SpringApplication(CalculateBestFpTpWeights.class);
        sa.setAdditionalProfiles(
                ComparingType.CHROMAPRINT.name(),
                ComparingType.PEAKS.name(),
                ComparingType.LASTFM.name(),
                "NEO4J"
        );
        ConfigurableApplicationContext context = sa.run(args);
        context.registerShutdownHook();
        CalculateBestFpTpWeights calculator = context.getBean(CalculateBestFpTpWeights.class);
        for(ComparingType type : Arrays.asList(
                //ComparingType.CHROMAPRINT//,
                ComparingType.LASTFM//,
                //ComparingType.PEAKS
        )){
            LOG.info("Optimal weight value for {} is {}", type, calculator.calculate(type));
        }
    }

    public int calculate(ComparingType type){
        //todo: get init high and low values from service
        int low = 10;
        int high = 10000;
        double startHigh = calculateFpTp(type, high);
        double startLow = calculateFpTp(type, low);
        if(startLow == 0){
            LOG.debug("There are not any false positive elements at all");
            return low;
        }
        if(startHigh != 0 && startHigh <= startLow){
            LOG.debug("Start high {} is less than start low {}", startHigh, startLow);
            return high;
        }
        return calculate(new State(type, high, startHigh, low, startLow));
    }


    private int calculate(State state){
        LOG.debug("Next iteration for {}", state);
        int candidate = state.getCandidate();
        LOG.debug("Next candidate is {}", candidate);
        if(candidate == state.low){
            LOG.debug("Low limit was reached");
            return state.low;
        }
        double current = calculateFpTp(state.type, candidate);
        LOG.debug("Value for candidate {} is {}", candidate, current);
        if(current == 0 || current > state.lowCurrent){
            LOG.debug("High level is moved to candidate");
            state.high = candidate;
            state.highCurrent = current;
        } else {
            LOG.debug("Low level is moved to candidate");
            state.low = candidate;
            state.lowCurrent = current;
        }
        state.iterationCount++;
        return calculate(state);
    }

    private double calculateFpTp(ComparingType type, int weight){
        Stats result = trackSimilarityStatsService.getResultDependsOnFingerprintType(type, weight);
        LOG.debug("Stats for {} is {}", weight, result);
        if(result.getTruePositive() == 0){
            return Double.MAX_VALUE;
        }
        return (double)result.getFalsePositive() / (double)result.getTruePositive();
    }

    private class State{

        ComparingType type;
        int high;
        double highCurrent;
        int low;
        double lowCurrent;
        int iterationCount;

        public State(ComparingType type, int high, double highCurrent, int low, double lowCurrent) {
            this.type = type;
            this.high = high;
            this.highCurrent = highCurrent;
            this.low = low;
            this.lowCurrent = lowCurrent;
            this.iterationCount = 0;
        }

        public int getCandidate(){
            return (high + low) / 2;
        }

        @Override
        public String toString() {
            return "State{" +
                    "type=" + type +
                    ", high=" + high +
                    ", highCurrent=" + highCurrent +
                    ", low=" + low +
                    ", lowCurrent=" + lowCurrent +
                    ", iterationCount=" + iterationCount +
                    '}';
        }
    }
}
