package angry1980.audio.config;

import angry1980.audio.Adapter;
import angry1980.audio.dao.*;
import angry1980.audio.fingerprint.*;
import angry1980.audio.model.ComparingType;
import angry1980.audio.model.Fingerprint;
import angry1980.audio.similarity.FindSimilarTracks;
import angry1980.audio.similarity.FindSimilarTracksImpl;
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
    public Calculator<Fingerprint> lastFMCalculator(){
        return new HashProcessCalculator(new LastFMProcessCreator(), adapter, ComparingType.LASTFM);
    }

    @Bean
    public FindSimilarTracks lastFMFindSimilarTracks(){
        return new FindSimilarTracksImpl(
                trackSimilarityDAO,
                lastFMGetOrCreateFingerprint(),
                lastFMFingerprintCalculator(),
                ComparingType.LASTFM
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
        return lastFMInvertedIndex();
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
