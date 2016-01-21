package angry1980.audio.config;

import angry1980.audio.Adapter;
import angry1980.audio.dao.*;
import angry1980.audio.fingerprint.*;
import angry1980.audio.model.FingerprintType;
import angry1980.audio.model.HashFingerprint;
import angry1980.audio.similarity.FindSimilarTracksImpl;
import angry1980.audio.similarity.HashErrorRatesCalculator;
import angry1980.audio.similarity.FindSimilarTracks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("CHROMAPRINT")
public class ChromaprintFingerprintConfig {

    @Autowired
    private Adapter adapter;
    @Autowired
    private TrackSimilarityDAO trackSimilarityDAO;
    @Autowired
    private TrackDAO trackDAO;

    @Bean
    public FingerprintDAO chromaprintFingerprintDAO(){
        return new FingerprintDAOInMemoryImpl<>();
    }

    @Bean
    public Calculator<HashFingerprint> chromaprintCalculator(){
        return new HashProcessCalculator(new ChromaprintProcessCreator(), adapter, FingerprintType.CHROMAPRINT);
    }

    @Bean
    public FindSimilarTracks chromaprintFindSimilarTracks(){
        return new FindSimilarTracksImpl(
                trackSimilarityDAO,
                chromaprintGetOrCreateFingerprint(),
                new HashErrorRatesCalculator(FingerprintType.CHROMAPRINT, trackDAO, chromaprintFingerprintDAO()),
                FingerprintType.CHROMAPRINT
        );
    }

    @Bean
    public GetOrCreateFingerprint chromaprintGetOrCreateFingerprint(){
        return new GetOrCreateFingerprint(
                chromaprintFingerprintDAO(),
                trackDAO,
                chromaprintCalculator(),
                new HashInvertedIndex(new TrackHashDAOInMemoryImpl())
        );
    }

}
