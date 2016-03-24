package angry1980.audio.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class JsonSerializer<T> implements Serializer<T>{

    private static Logger LOG = LoggerFactory.getLogger(JsonSerializer.class);

    private ObjectMapper objectMapper = new ObjectMapper();
    private byte[] empty = "".getBytes();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

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
