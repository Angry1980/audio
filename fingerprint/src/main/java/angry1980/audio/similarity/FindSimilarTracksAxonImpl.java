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
import rx.subjects.ReplaySubject;

import java.util.Objects;

public class FindSimilarTracksAxonImpl implements FindSimilarTracks {

    private static Logger LOG = LoggerFactory.getLogger(FindSimilarTracksAxonImpl.class);

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
                .orElseGet(() -> {
                    ReplaySubject<EventMessage> subject = ReplaySubject.create();
                    EventListener listener = subject::onNext;
                    eventBus.subscribe(listener);
                    LOG.debug("TrackSimilarityCalculatedEvent listener was added");
                    commandGateway.send(
                            new GenericCommandMessage<>(
                                    ImmutableCalculateTrackSimilarityCommand.class.getName() + type.getFingerprintType().name(),
                                    ImmutableCalculateTrackSimilarityCommand.builder().track(track).type(type).build(),
                                    MetaData.emptyInstance()
                            ),
                            new CommandCallback<Object>() {
                                @Override
                                public void onSuccess(Object result) {
                                    subject.onCompleted();
                                    LOG.debug("Stop track similarity calculation for {} {}", track.getId(), type);
                                    clear();
                                }

                                @Override
                                public void onFailure(Throwable cause) {
                                    subject.onError(cause);
                                    LOG.debug("Error while track similarity calculation for {} {}", track.getId(), type);
                                    clear();
                                }

                                private void clear(){
                                    eventBus.unsubscribe(listener);
                                    LOG.debug("TrackSimilarityCalculatedEvent listener was removed");
                                }
                            }
                    );
                    Observable<TrackSimilarity> o = subject
                            .map(EventMessage::getPayload)
                            .cast(TrackSimilarityCalculatedEvent.class)
                            .map(TrackSimilarityCalculatedEvent::getSimilarity)
                            .filter(s -> track.getId() == s.getTrack1() && type.equals(s.getComparingType()))
                            .doOnNext(e -> LOG.debug("Track similarity event for {} {}", track.getId(), type))
                            ;
                    //o.map(Object::toString).subscribeOn(Schedulers.newThread()).subscribe(LOG::debug);
                    return o;
                });
    }

    @Override
    public String toString() {
        return "FindSimilarTracks{" +
                "fingerprintType=" + fingerprintType +
                '}';
    }

}
