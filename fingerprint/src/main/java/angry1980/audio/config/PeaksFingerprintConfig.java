package angry1980.audio.config;

import angry1980.audio.Adapter;
import angry1980.audio.dao.*;
import angry1980.audio.fingerprint.GetOrCreateFingerprint;
import angry1980.audio.fingerprint.PeaksCalculator;
import angry1980.audio.fingerprint.PeaksInvertedIndex;
import angry1980.audio.model.FingerprintType;
import angry1980.audio.similarity.FindSimilarTracks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class PeaksFingerprintConfig {

    @Autowired
    private TrackSimilarityDAO trackSimilarityDAO;
    @Autowired
    private TrackDAO trackDAO;
    @Autowired
    private Adapter adapter;

    @Bean
    public PeaksInvertedIndex peaksInvertedIndex(){
        return new PeaksInvertedIndex(peakDAO());
    }

    @Bean
    public PeakDAO peakDAO(){
        return new PeakDAOInMemoryImpl();
    }

    @Bean
    public FindSimilarTracks peaksFindSimilarTracks(){
        return new FindSimilarTracks(
                trackSimilarityDAO,
                peaksGetOrCreateFingerprint(),
                peaksInvertedIndex(),
                FingerprintType.PEAKS
        );
    }

    @Bean
    public GetOrCreateFingerprint peaksGetOrCreateFingerprint(){
        return new GetOrCreateFingerprint(
                new FingerprintDAOFakeImpl(),
                trackDAO,
                new PeaksCalculator(adapter),
                peaksInvertedIndex()
        );
    }
}
