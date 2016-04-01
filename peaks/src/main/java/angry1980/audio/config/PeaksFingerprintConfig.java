package angry1980.audio.config;

import angry1980.audio.Adapter;
import angry1980.audio.dao.*;
import angry1980.audio.fingerprint.GetOrCreateFingerprint;
import angry1980.audio.fingerprint.HashInvertedIndex;
import angry1980.audio.fingerprint.PeaksCalculator;
import angry1980.audio.model.Fingerprint;
import angry1980.audio.model.FingerprintType;
import angry1980.audio.similarity.*;
import angry1980.audio.track.TrackAggregator;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.repository.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class PeaksFingerprintConfig {

    @Autowired
    private TrackSimilarityDAO trackSimilarityDAO;
    @Autowired
    private Adapter adapter;
    @Autowired
    private CommandGateway commandGateway;
    @Autowired
    private EventBus eventBus;
    @Autowired
    private CommandBus commandBus;
    @Autowired
    private Repository<TrackAggregator> trackRepository;

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
        return new FindSimilarTracksAxonImpl(
                trackSimilarityDAO,
                FingerprintType.PEAKS,
                commandGateway,
                eventBus
        );
    }

    @Bean
    public CalculateTrackSimilarityCommandHandler peaksCalculateTrackSimilarityCommandHandler(){
        CalculateTrackSimilarityCommandHandler handler = new CalculateTrackSimilarityCommandHandler(
                new FindSimilarTracksImpl(
                        trackSimilarityDAO,
                        peaksGetOrCreateFingerprint(),
                        peaksFingerprintCalculator(),
                        FingerprintType.PEAKS
                ),
                trackRepository
        );
        commandBus.subscribe(ImmutableCalculateTrackSimilarityCommand.class.getName(), handler);
        return handler;
    }

    @Bean
    public GetOrCreateFingerprint peaksGetOrCreateFingerprint(){
        return new GetOrCreateFingerprint(
                peaksFingerprintDAO(),
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
