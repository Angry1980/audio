package angry1980.utils;

import java.util.Map;
import java.util.Objects;

public class SpringMapWrapper<K, V> {

    private final Map<K,V> map;

    public SpringMapWrapper(Map<K, V> map) {
        this.map = Objects.requireNonNull(map);
    }

    public Map<K, V> getMap() {
        return map;
    }
}
