package angry1980.audio.config;

import angry1980.audio.Adapter;
import angry1980.audio.dao.*;
import angry1980.audio.fingerprint.*;
import angry1980.audio.model.FingerprintType;
import angry1980.audio.model.HashFingerprint;
import angry1980.audio.similarity.FindSimilarTracks;
import angry1980.audio.similarity.FindSimilarTracksImpl;
import angry1980.audio.similarity.HashErrorRatesCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

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
    public Calculator<HashFingerprint> lastFMCalculator(){
        return new HashProcessCalculator(new LastFMProcessCreator(), adapter, FingerprintType.LASTFM);
    }

    @Bean
    public FindSimilarTracks lastFMFindSimilarTracks(){
        return new FindSimilarTracksImpl(
                trackSimilarityDAO,
                lastFMGetOrCreateFingerprint(),
                new HashErrorRatesCalculator(FingerprintType.LASTFM, trackDAO, lastFMFingerprintDAO()),
                FingerprintType.LASTFM
        );
    }

    @Bean
    public GetOrCreateFingerprint lastFMGetOrCreateFingerprint(){
        return new GetOrCreateFingerprint(
                lastFMFingerprintDAO(),
                trackDAO,
                lastFMCalculator(),
                new HashInvertedIndex(lastFMTrackHashDAO())
        );
    }

    @Bean
    public TrackHashDAO lastFMTrackHashDAO(){
        return new TrackHashDAOInMemoryImpl();
    }
}
