package angry1980.audio.config;

import angry1980.audio.Adapter;
import angry1980.audio.dao.*;
import angry1980.audio.fingerprint.*;
import angry1980.audio.fingerprint.Calculator;
import angry1980.audio.model.Fingerprint;
import angry1980.audio.model.FingerprintType;
import angry1980.audio.similarity.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Arrays;

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
    public Calculator<Fingerprint> chromaprintCalculator(){
        return new HashProcessCalculator(new ChromaprintProcessCreator(), adapter, FingerprintType.CHROMAPRINT);
    }

    @Bean
    public FindSimilarTracks chromaprintFindSimilarTracks(){
        return new FindSimilarTracksImpl(
                trackSimilarityDAO,
                chromaprintGetOrCreateFingerprint(),
                chromaprintSimilarityCalculator(),
                FingerprintType.CHROMAPRINT
        );
    }

    @Bean
    public GetOrCreateFingerprint chromaprintGetOrCreateFingerprint(){
        return new GetOrCreateFingerprint(
                chromaprintFingerprintDAO(),
                trackDAO,
                chromaprintCalculator(),
                chromaprintInvertedIndex()
        );
    }

    @Bean
    public angry1980.audio.similarity.Calculator<Fingerprint> chromaprintSimilarityCalculator(){
        return new ComplexCalculator<>(
                Arrays.asList(
                        chromaprintInvertedIndex(),
                        chromaprintErrorRatesSimilarityCalculator()
                )
        );
    }

    @Bean
    public angry1980.audio.similarity.Calculator<Fingerprint> chromaprintErrorRatesSimilarityCalculator(){
        return new HashErrorRatesCalculator(
                new HashErrorRatesCalculatorTrackSourceImpl(trackDAO),
                chromaprintFingerprintDAO()
        );
    }

    @Bean
    public HashInvertedIndex chromaprintInvertedIndex(){
        return new HashInvertedIndex(0.005, 0.01, chromaprintTrackHashDAO()).setSilenceHash(chromaprintSilenceHash());
    }

    @Bean
    public TrackHashDAO chromaprintTrackHashDAO(){
        return new TrackHashDAOInMemoryImpl();
    }

    @Bean
    public Integer chromaprintSilenceHash(){
        return 2012835109;
    }
}
