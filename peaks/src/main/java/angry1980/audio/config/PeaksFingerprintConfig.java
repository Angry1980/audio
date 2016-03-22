package angry1980.audio.config;

import angry1980.audio.Adapter;
import angry1980.audio.dao.*;
import angry1980.audio.fingerprint.GetOrCreateFingerprint;
import angry1980.audio.fingerprint.HashInvertedIndex;
import angry1980.audio.fingerprint.PeaksCalculator;
import angry1980.audio.model.Fingerprint;
import angry1980.audio.model.FingerprintType;
import angry1980.audio.similarity.Calculator;
import angry1980.audio.similarity.FindSimilarTracks;
import angry1980.audio.similarity.FindSimilarTracksImpl;
import angry1980.audio.similarity.InvertedIndexCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class PeaksFingerprintConfig {

    @Autowired
    private TrackSimilarityDAO trackSimilarityDAO;
    @Autowired
    private TrackDAO trackDAO;
    @Autowired
    private Adapter adapter;

    @Bean
    public HashInvertedIndex peaksInvertedIndex(){
        return new HashInvertedIndex(peakDAO(), Optional.empty());
    }

    @Bean
    public TrackHashDAO peakDAO(){
        return new TrackHashDAOInMemoryImpl();
    }

    @Bean
    public FindSimilarTracks peaksFindSimilarTracks(){
        return new FindSimilarTracksImpl(
                trackSimilarityDAO,
                peaksGetOrCreateFingerprint(),
                peaksFingerprintCalculator(),
                FingerprintType.PEAKS
        );
    }

    @Bean
    public GetOrCreateFingerprint peaksGetOrCreateFingerprint(){
        return new GetOrCreateFingerprint(
                peaksFingerprintDAO(),
                trackDAO,
                peaksCalculator(),
                peaksInvertedIndex()
        );
    }

    @Bean
    public PeaksCalculator peaksCalculator(){
        return new PeaksCalculator(adapter);
    }

    @Bean
    public Calculator<Fingerprint> peaksFingerprintCalculator(){
        return new InvertedIndexCalculator(0.01, 0.01, peaksInvertedIndex());
    }

    @Bean
    public FingerprintDAO peaksFingerprintDAO(){
        return new FingerprintDAOFakeImpl();
    }
}
