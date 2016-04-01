package angry1980.audio.similarity;

import angry1980.audio.model.TrackSimilarity;
import angry1980.audio.track.TrackAggregator;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.repository.Repository;
import org.axonframework.unitofwork.UnitOfWork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Subscriber;

import java.util.Objects;

public class CalculateTrackSimilarityCommandHandler implements CommandHandler<ImmutableCalculateTrackSimilarityCommand>{

    private static Logger LOG = LoggerFactory.getLogger(CalculateTrackSimilarityCommandHandler.class);

    private FindSimilarTracks findSimilarTracks;
    private Repository<TrackAggregator> repository;

    public CalculateTrackSimilarityCommandHandler(FindSimilarTracks findSimilarTracks,
                                                  Repository<TrackAggregator> repository) {
        this.repository = Objects.requireNonNull(repository);
        this.findSimilarTracks = Objects.requireNonNull(findSimilarTracks);
    }

    @Override
    public Object handle(CommandMessage<ImmutableCalculateTrackSimilarityCommand> commandMessage, UnitOfWork unitOfWork) throws Throwable {
        ImmutableCalculateTrackSimilarityCommand command = commandMessage.getPayload();
        findSimilarTracks.apply(command.getTrack(), command.getType())
                .subscribe(new Subscriber<TrackSimilarity>() {

                    private TrackAggregator aggregator;

                    @Override
                    public void onStart() {
                        LOG.debug("Start calculation of {} similarities for track {}", command.getType(), command.getTrack().getId());
                        aggregator = repository.load(command.getTrackId());
                    }

                    @Override
                    public void onCompleted() {
                        LOG.debug("Finish similarities calculating for {}", command.getTrack().getId());
                    }

                    @Override
                    public void onError(Throwable e) {
                        LOG.debug("Error while calculating similarities for {}: {}", command.getTrack().getId(), e);
                    }

                    @Override
                    public void onNext(TrackSimilarity o) {
                        LOG.debug("Add {} to aggregator of track {}", o, aggregator.getIdentifier());
                        aggregator.addSimilarity(o);
                    }

                });
        return null;
    }

}
