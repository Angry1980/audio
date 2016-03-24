package angry1980.audio.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public class JsonDeserializer<T> implements Deserializer<T> {

    private static Logger LOG = LoggerFactory.getLogger(JsonSerializer.class);

    private ObjectMapper objectMapper;
    private Class<T> dataType;

    public JsonDeserializer(Class<T> dataType) {
        this.dataType = Objects.requireNonNull(dataType);
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        try {
            return objectMapper.readValue(data, dataType);
        } catch (IOException e) {
            LOG.error("Json processing failed for object{}: {}", data.toString(), e.getMessage());
        }
        return null;

    }

    @Override
    public void close() {

    }
}
