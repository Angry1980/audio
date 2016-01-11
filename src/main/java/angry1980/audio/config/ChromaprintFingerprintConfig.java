package angry1980.audio.config;

import angry1980.audio.dao.*;
import angry1980.audio.fingerprint.*;
import angry1980.audio.model.FingerprintType;
import angry1980.audio.similarity.ChromaprintErrorRatesCalculator;
import angry1980.audio.similarity.FindSimilarTracks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChromaprintFingerprintConfig {

    @Autowired
    private TrackSimilarityDAO trackSimilarityDAO;
    @Autowired
    private TrackDAO trackDAO;

    @Bean
    public FingerprintDAO chromaprintFinferprintDAO(){
        return new FingerprintDAOInMemoryImpl<>();
    }

    @Bean
    public FindSimilarTracks chromaprintFindSimilarTracks(){
        return new FindSimilarTracks(
                trackSimilarityDAO,
                chromaprintGetOrCreateFingerprint(),
                new ChromaprintErrorRatesCalculator(chromaprintFinferprintDAO()),
                FingerprintType.CHROMAPRINT
        );
    }

    @Bean
    public GetOrCreateFingerprint chromaprintGetOrCreateFingerprint(){
        return new GetOrCreateFingerprint(
                chromaprintFinferprintDAO(),
                trackDAO,
                new ChromaprintCalculator(),
                new ChromaprintInvertedIndex(new ChromaprintHashDAOInMemoryImpl())
        );
    }

}
