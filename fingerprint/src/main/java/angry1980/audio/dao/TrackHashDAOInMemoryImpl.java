package angry1980.audio.dao;

import angry1980.audio.model.TrackHash;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.*;

public class TrackHashDAOInMemoryImpl implements TrackHashDAO {

    private final long mask;
    private final Long2ObjectMap<List<TrackHash>> index;

    public TrackHashDAOInMemoryImpl(){
        this(-1);
    }

    public TrackHashDAOInMemoryImpl(long mask){
        this.index = new Long2ObjectOpenHashMap<>();
        this.mask = mask;
    }

    @Override
    public Optional<TrackHash> create(TrackHash hash) {
        if(hash != null){
            index.computeIfAbsent(getKey(hash.getHash()), l -> new ArrayList()).add(hash);
        }
        return Optional.ofNullable(hash);
    }

    @Override
    public List<TrackHash> findByHash(long hash) {
        return index.getOrDefault(getKey(hash), Collections.emptyList());
    }

    private long getKey(long hash){
        if(mask == -1){
            return hash;
        }
        return hash & mask;
    }
}
