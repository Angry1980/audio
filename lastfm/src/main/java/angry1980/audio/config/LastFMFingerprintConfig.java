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
@Profile("LASTFM")
public class LastFMFingerprintConfig {

    @Autowired
    private Adapter adapter;
    @Autowired
    private TrackSimilarityDAO trackSimilarityDAO;
    @Autowired
    private TrackDAO trackDAO;

    @Bean
    public FingerprintDAO lastFMFingerprintDAO(){
        return new FingerprintDAOInMemoryImpl<>();
    }

    @Bean
    public Calculator<Fingerprint> lastFMCalculator(){
        return new HashProcessCalculator(new LastFMProcessCreator(), adapter, FingerprintType.LASTFM);
    }

    @Bean
    public FindSimilarTracks lastFMFindSimilarTracks(){
        return new FindSimilarTracksImpl(
                trackSimilarityDAO,
                lastFMGetOrCreateFingerprint(),
                lastFMFingerprintCalculator(),
                FingerprintType.LASTFM
        );
    }

    @Bean
    public GetOrCreateFingerprint lastFMGetOrCreateFingerprint(){
        return new GetOrCreateFingerprint(
                lastFMFingerprintDAO(),
                trackDAO,
                lastFMCalculator(),
                lastFMInvertedIndex()
        );
    }

    @Bean
    public angry1980.audio.similarity.Calculator<Fingerprint> lastFMFingerprintCalculator(){
        return new ComplexCalculator<>(
                Arrays.asList(
                        lastFMInvertedIndex(),
                        lastFMErrorRatesSimilarityCalculator()
                )
        );

    }

    @Bean
    public angry1980.audio.similarity.Calculator<Fingerprint> lastFMErrorRatesSimilarityCalculator(){
        return new HashErrorRatesCalculator(
                new HashErrorRatesCalculatorTrackSourceImpl(trackDAO),
                lastFMFingerprintDAO()
        );
    }

    @Bean
    public HashInvertedIndex lastFMInvertedIndex(){
        return new HashInvertedIndex(10, 10, lastFMTrackHashDAO());
    }

    @Bean
    public TrackHashDAO lastFMTrackHashDAO(){
        return new TrackHashDAOInMemoryImpl();
    }
}
