package angry1980.audio.axon;

import org.axonframework.domain.EventMessage;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.EventListener;

public class KafkaEventBus implements EventBus{

    @Override
    public void publish(EventMessage... eventMessages) {

    }

    @Override
    public void subscribe(EventListener eventListener) {

    }

    @Override
    public void unsubscribe(EventListener eventListener) {

    }
}
