package angry1980.audio.config;

import angry1980.audio.Adapter;
import angry1980.audio.dao.*;
import angry1980.audio.fingerprint.*;
import angry1980.audio.fingerprint.Calculator;
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
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;

@Configuration
@PropertySource({"classpath:chromaprint.properties"})
public class ChromaprintFingerprintConfig {

    @Autowired
    private Adapter adapter;
    @Autowired
    private TrackSimilarityDAO trackSimilarityDAO;
    @Autowired
    private TrackDAO trackDAO;
    @Autowired
    private Environment env;
    @Autowired
    private CommandGateway commandGateway;
    @Autowired
    private EventBus eventBus;
    @Autowired
    private CommandBus commandBus;
    @Autowired
    private Repository<TrackAggregator> trackRepository;

    @Bean
    public FingerprintDAO chromaprintFingerprintDAO(){
        return new FingerprintDAOInMemoryImpl<>();
    }

    @Bean
    public Calculator<Fingerprint> chromaprintCalculator(){
        return new HashProcessCalculator(
                new ChromaprintProcessCreator(Optional.of(new File(env.getProperty("music.chromaprint.fpcalc.folder"))).filter(f -> f.isDirectory())),
                adapter,
                FingerprintType.CHROMAPRINT
        );
    }

    @Bean
    public FindSimilarTracks chromaprintFindSimilarTracks(){
        return new FindSimilarTracksAxonImpl(
                trackSimilarityDAO,
                FingerprintType.CHROMAPRINT,
                commandGateway,
                eventBus
        );
    }

    @Bean
    public CalculateTrackSimilarityCommandHandler chromaprintCalculateTrackSimilarityCommandHandler(){
        CalculateTrackSimilarityCommandHandler handler = new CalculateTrackSimilarityCommandHandler(
                new FindSimilarTracksImpl(
                        trackSimilarityDAO,
                        chromaprintGetOrCreateFingerprint(),
                        chromaprintSimilarityCalculator(),
                        FingerprintType.CHROMAPRINT
                ),
                trackRepository
        );
        commandBus.subscribe(ImmutableCalculateTrackSimilarityCommand.class.getName() + FingerprintType.CHROMAPRINT.name(), handler);
        return handler;
    }

    @Bean
    public GetOrCreateFingerprint chromaprintGetOrCreateFingerprint(){
        return new GetOrCreateFingerprint(
                chromaprintFingerprintDAO(),
                chromaprintCalculator(),
                chromaprintInvertedIndex()
        );
    }

    @Bean
    public angry1980.audio.similarity.Calculator<Fingerprint> chromaprintSimilarityCalculator(){
        return new CompositeCalculator<>(
                Arrays.asList(
                        chromaprintInvertedIndexCalculator(),
                        chromaprintErrorRatesSimilarityCalculator()
                )
        );
    }

    @Bean
    public angry1980.audio.similarity.Calculator<Fingerprint> chromaprintInvertedIndexCalculator(){
        return new InvertedIndexCalculator(0.005, 0.01, chromaprintInvertedIndex());
    }

    @Bean
    public angry1980.audio.similarity.Calculator<Fingerprint> chromaprintErrorRatesSimilarityCalculator(){
        return new HashErrorRatesCalculator(
                chromaprintHashErrorRatesCalculatorTrackSource(),
                chromaprintFingerprintDAO()
        );
    }

    @Bean
    public HashErrorRatesCalculatorTrackSource chromaprintHashErrorRatesCalculatorTrackSource(){
        return new HashErrorRatesCalculatorTrackSourceImpl(trackDAO);
        //solution of unresolvable circular reference
        //return new HashErrorRatesCalculatorTrackSourceProxy(context, ComparingType.LASTFM);
    }

    @Bean
    public HashInvertedIndex chromaprintInvertedIndex(){
        return new HashInvertedIndex(chromaprintTrackHashDAO(), Optional.ofNullable(chromaprintSilenceHash()));
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
