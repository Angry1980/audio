package angry1980.audio.stats;

import angry1980.audio.AppConfig;
import angry1980.audio.neo4j.Neo4jDAOConfig;
import angry1980.audio.model.ComparingType;
import angry1980.audio.service.TrackSimilarityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Arrays;

@Configuration
@Import(value = {AppConfig.class, Neo4jDAOConfig.class})
public class CalculateSimilaritiesHistWeights implements CalculateSimilaritiesWeights {

    @Autowired
    private TrackSimilarityService trackSimilarityService;

    public static void main(String[] args){
        CalculateSimilaritiesWeights.init(args, CalculateSimilaritiesHistWeights.class);
    }

    @Override
    public void calculate(){
        new HistHandler(Arrays.asList(ComparingType.CHROMAPRINT)).handle(
                (onlyTruthPositive, type) -> trackSimilarityService.findSimilarities(type, onlyTruthPositive)
        );
    }

}
