package angry1980.audio;

import angry1980.audio.service.TrackSimilarityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(Neo4jDAOConfig.class)
public class CalculateUniqieSimilaritiesHistWeights implements CalculateSimilaritiesWeights{
    @Autowired
    private TrackSimilarityService trackSimilarityService;

    public static void main(String[] args){
        CalculateSimilaritiesWeights.init(args, CalculateCommonSimilaritiesHistWeights.class);
    }

    @Override
    public void calculate(){
        new HistHandler(trackSimilarityService).handle(
                (truthPositive, type) -> trackSimilarityService.findUniqueSimilarities(type, truthPositive)
        );
    }

}
