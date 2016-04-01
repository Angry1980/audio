package angry1980.audio.similarity;

import angry1980.audio.model.ComparingType;
import angry1980.audio.model.Track;
import angry1980.audio.model.TrackSimilarity;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.domain.EventMessage;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class TrackSimilarityEventListenerBuilder{

    private static Logger LOG = LoggerFactory.getLogger(TrackSimilarityEventListenerBuilder.class);

    public static TrackSimilarityEventListenerBuilder create(Track track, ComparingType type){
        return new TrackSimilarityEventListenerBuilder(track, type);
    }

    private final Track track;
    private final ComparingType type;

    private TrackSimilarityEventListenerBuilder(Track track, ComparingType type) {
        this.track = track;
        this.type = type;
    }

    public Observable.OnSubscribe<TrackSimilarity> build(EventBus eventBus, CommandGateway commandGateway){
        TrackSimilarityEventListener listener = new TrackSimilarityEventListener(commandGateway);
        eventBus.subscribe(listener);
        listener.setEventUnsubscriber(n -> eventBus.unsubscribe(listener));
        return listener;
    }

    private class TrackSimilarityEventListener implements Observable.OnSubscribe<TrackSimilarity>, EventListener{

        private Consumer<Void> eventUnsubscriber;
        private CommandGateway commandGateway;
        private AtomicBoolean sent;
        private List<Subscriber<? super TrackSimilarity>> subscribers;

        public TrackSimilarityEventListener(CommandGateway commandGateway){
            this.subscribers = new ArrayList<>();
            this.sent = new AtomicBoolean(false);
            this.commandGateway = commandGateway;
        }

        public void setEventUnsubscriber(Consumer<Void> eventUnsubscriber) {
            this.eventUnsubscriber = eventUnsubscriber;
        }

        @Override
        public void call(Subscriber<? super TrackSimilarity> subscriber) {
            //todo: refactor, use back pressure
            LOG.debug("Add subscriber of event stream of track {}", track.getId());
            subscribers.add(subscriber);
            if(sent.compareAndSet(false, true)){
                commandGateway.send(ImmutableCalculateTrackSimilarityCommand.builder().track(track).type(type).build(), new Callback());
            }
        }

        @Override
        public void handle(EventMessage eventMessage) {
            if(TrackSimilarityCalculatedEvent.class.isAssignableFrom(eventMessage.getPayloadType())){
                on((TrackSimilarityCalculatedEvent)eventMessage.getPayload());
            }
        }

        public void on(TrackSimilarityCalculatedEvent event){
            if(track.getId() != event.getSimilarity().getTrack1()
                    || !type.equals(event.getSimilarity().getComparingType())){
                return;
            }
            LOG.debug("Track similarity event for {} {}", track.getId(), type);
            subscribers.stream()
                    .filter(s -> !s.isUnsubscribed())
                    .forEach(s -> s.onNext(event.getSimilarity()));
        }

        private class Callback implements CommandCallback<Object>{

            @Override
            public void onSuccess(Object o) {
                LOG.debug("Stop track similarity calculation for {} {}", track.getId(), type);
                subscribers.stream()
                        .filter(s -> !s.isUnsubscribed())
                        .forEach(s -> s.onCompleted());
                eventUnsubscriber.accept(null);
            }

            @Override
            public void onFailure(Throwable throwable) {
                LOG.debug("Stop track similarity calculation for {} {}", track.getId(), type);
                subscribers.stream()
                        .filter(s -> !s.isUnsubscribed())
                        .forEach(s -> s.onError(throwable));
                eventUnsubscriber.accept(null);
            }
        }

    }

}
