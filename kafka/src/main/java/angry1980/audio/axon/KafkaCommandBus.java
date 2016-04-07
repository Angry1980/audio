package angry1980.audio.axon;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.CommandMessage;

public class KafkaCommandBus implements CommandBus{

    @Override
    public void dispatch(CommandMessage<?> commandMessage) {

    }

    @Override
    public <R> void dispatch(CommandMessage<?> commandMessage, CommandCallback<R> commandCallback) {

    }

    @Override
    public <C> void subscribe(String s, CommandHandler<? super C> commandHandler) {

    }

    @Override
    public <C> boolean unsubscribe(String s, CommandHandler<? super C> commandHandler) {
        return false;
    }
}
