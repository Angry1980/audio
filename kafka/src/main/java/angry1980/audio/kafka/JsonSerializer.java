package angry1980.audio.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public class JsonSerializer<T> implements Serializer<T>, Deserializer<T>{

    private static Logger LOG = LoggerFactory.getLogger(JsonSerializer.class);

    private ObjectMapper objectMapper = new ObjectMapper();
    private byte[] empty = "".getBytes();
    private Class<T> dataType;

    public JsonSerializer(Class<T> dataType) {
        this.dataType = Objects.requireNonNull(dataType);
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
    public byte[] serialize(String topic, T data) {
        try {
            return objectMapper.writeValueAsString(data).getBytes();
        } catch (JsonProcessingException e) {
            LOG.error("Json processing failed for object {}: {}", data.getClass().getName(), e.getMessage());
        }
        return empty;
    }

    @Override
    public void close() {

    }
}
