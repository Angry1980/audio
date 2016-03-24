package angry1980.audio.kafka;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.kafka.common.serialization.Deserializer;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableConsumerProperties.class)
public interface ConsumerProperties {

    Class<? extends Deserializer> getValueDeserializer();

    String getTopicName();

    @Value.Default
    default String getGroupName(){
        return "default";
    }
}
