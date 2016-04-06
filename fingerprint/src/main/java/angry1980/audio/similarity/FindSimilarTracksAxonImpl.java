package angry1980.audio.similarity;

import angry1980.audio.dao.TrackSimilarityDAO;
import angry1980.audio.model.*;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.GenericCommandMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.domain.EventMessage;
import org.axonframework.domain.MetaData;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.Subscriptions;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class FindSimilarTracksAxonImpl implements FindSimilarTracks {

    private static Logger LOG = LoggerFactory.getLogger(FindSimilarTracksImpl.class);

    private TrackSimilarityDAO trackSimilarityDAO;
    private FingerprintType fingerprintType;
    private CommandGateway commandGateway;
    private EventBus eventBus;

    public FindSimilarTracksAxonImpl(TrackSimilarityDAO trackSimilarityDAO,
                                     FingerprintType fingerprintType,
                                     CommandGateway commandGateway,
                                     EventBus eventBus) {
        this.trackSimilarityDAO = Objects.requireNonNull(trackSimilarityDAO);
        this.fingerprintType = Objects.requireNonNull(fingerprintType);
        this.commandGateway = Objects.requireNonNull(commandGateway);
        this.eventBus = Objects.requireNonNull(eventBus);
    }

    @Override
    public boolean test(FingerprintType fingerprintType) {
        return this.fingerprintType.equals(fingerprintType);
    }

    @Override
    public Observable<TrackSimilarity> apply(Track track, ComparingType type) {
        return trackSimilarityDAO.findByTrackIdAndFingerprintType(track.getId(), type)
                .map(Observable::from)
                .orElseGet(() ->
                        Observable.create(new OnSubscribeImpl(track, type, eventBus, commandGateway))
                                .filter(em -> TrackSimilarityCalculatedEvent.class.isAssignableFrom(em.getPayloadType()))
                                .map(em -> (TrackSimilarityCalculatedEvent)em.getPayload())
                                .filter(e -> track.getId() == e.getSimilarity().getTrack1() && type.equals(e.getSimilarity().getComparingType()))
                                .doOnNext(e -> LOG.debug("Track similarity event for {} {}", track.getId(), type))
                                .map(TrackSimilarityCalculatedEvent::getSimilarity)
                );
    }

    @Override
    public String toString() {
        return "FindSimilarTracks{" +
                "fingerprintType=" + fingerprintType +
                '}';
    }

    private static class OnSubscribeImpl implements Observable.OnSubscribe<EventMessage>{

        private Track track;
        private ComparingType type;
        private EventBus eventBus;
        private CommandGateway commandGateway;
        private AtomicBoolean sent;
        private Callback callback;

        public OnSubscribeImpl(Track track, ComparingType type, EventBus eventBus, CommandGateway commandGateway) {
            this.track = track;
            this.type = type;
            this.eventBus = eventBus;
            this.commandGateway = commandGateway;
            this.sent = new AtomicBoolean(false);
            this.callback = new Callback(track, type);
        }

        @Override
        public void call(Subscriber<? super EventMessage> subscriber) {
            EventListener listener = subscriber::onNext;
            eventBus.subscribe(listener);
            callback.add(subscriber);
            LOG.debug("TrackSimilarityCalculatedEvent listener was added");
            subscriber.add(Subscriptions.create(() -> {
                eventBus.unsubscribe(listener);
                LOG.debug("TrackSimilarityCalculatedEvent listener was removed");
            }));
            if(sent.compareAndSet(false, true)){
                commandGateway.send(
                        new GenericCommandMessage<>(
                                ImmutableCalculateTrackSimilarityCommand.class.getName() + type.getFingerprintType().name(),
                                ImmutableCalculateTrackSimilarityCommand.builder().track(track).type(type).build(),
                                MetaData.emptyInstance()
                        ),
                        callback
                );
            }

        }
    }

    private static class Callback implements CommandCallback<Object> {

        private Track track;
        private ComparingType type;
        private CopyOnWriteArrayList<Subscriber> subscribers = new CopyOnWriteArrayList<>();

        public Callback(Track track, ComparingType type) {
            this.track = track;
            this.type = type;
        }

        public void add(Subscriber subscriber){
            subscribers.add(subscriber);
        }

        @Override
        public void onSuccess(Object o) {
            LOG.debug("Stop track similarity calculation for {} {}", track.getId(), type);
            handle(Subscriber::onCompleted);
        }

        @Override
        public void onFailure(Throwable throwable) {
            LOG.debug("Stop track similarity calculation for {} {}", track.getId(), type);
            handle(s -> s.onError(throwable));
        }

        private void handle(Consumer<Subscriber> c){
            subscribers.stream()
                    .filter(s -> !s.isUnsubscribed())
                    .forEach(c);
        }
    }

}
