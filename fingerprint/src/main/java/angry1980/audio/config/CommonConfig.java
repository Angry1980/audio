package angry1980.audio.config;

import angry1980.audio.dao.TrackDAO;
import angry1980.audio.dao.TrackSimilarityDAO;
import angry1980.audio.service.TrackSimilarityServiceImpl;
import angry1980.audio.similarity.*;
import angry1980.audio.track.TrackAggregator;
import angry1980.audio.track.TrackStorageEventListener;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.commandhandling.annotation.AggregateAnnotationCommandHandler;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.commandhandling.gateway.DefaultCommandGateway;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.SimpleEventBus;
import org.axonframework.eventhandling.annotation.AnnotationEventListenerAdapter;
import org.axonframework.eventsourcing.EventSourcingRepository;
import org.axonframework.eventstore.EventStore;
import org.axonframework.eventstore.fs.FileSystemEventStore;
import org.axonframework.eventstore.fs.SimpleEventFileResolver;
import org.axonframework.repository.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.List;
import java.util.Optional;

@Configuration
public class CommonConfig {

    @Autowired
    private TrackDAO trackDAO;
    @Autowired
    private TrackSimilarityDAO trackSimilarityDAO;
    @Autowired
    private Optional<List<FindSimilarTracks>> findSimilarTracksList;

    @Bean
    public TrackSimilarityServiceImpl trackSimilarityService(){
        return new TrackSimilarityServiceImpl(
                new CompositeFindSimilarTracks(findSimilarTracksList),
                trackDAO,
                trackSimilarityDAO,
                tracksToCalculate()
        );
    }

    @Bean
    @ConditionalOnMissingBean(TracksToCalculate.class)
    public TracksToCalculate tracksToCalculate(){
        return new TracksToCalculateImpl(trackDAO);
    }

    @Bean
    public CommandBus commandBus(){
        return new SimpleCommandBus();
    }

    @Bean
    public CommandGateway commandGateway(){
        return new DefaultCommandGateway(commandBus());
    }

    @Bean
    public EventStore eventStore(){
        return new FileSystemEventStore(new SimpleEventFileResolver(new File("./events")));
    }

    @Bean
    public EventBus eventBus(){
        return new SimpleEventBus();
    }

    @Bean
    public Repository<TrackAggregator> trackRepository(){
        EventBus eventBus = eventBus();
        EventSourcingRepository<TrackAggregator> repository = new EventSourcingRepository<>(TrackAggregator.class, eventStore());
        repository.setEventBus(eventBus);
        AggregateAnnotationCommandHandler.subscribe(TrackAggregator.class, repository, commandBus());
        AnnotationEventListenerAdapter.subscribe(trackStorageEventListener(), eventBus);
        return repository;
    }

    @Bean
    public TrackStorageEventListener trackStorageEventListener(){
        return new TrackStorageEventListener(trackDAO);
    }

}
