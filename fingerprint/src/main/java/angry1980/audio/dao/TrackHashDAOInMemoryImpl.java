package angry1980.audio.dao;

import angry1980.audio.model.TrackHash;

import java.util.*;

public class TrackHashDAOInMemoryImpl implements TrackHashDAO {

    private long mask;
    private Map<Long, List<TrackHash>> index;

    public TrackHashDAOInMemoryImpl(){
        this(-1);
    }

    public TrackHashDAOInMemoryImpl(long mask){
        this.index = new HashMap<>();
        this.mask = mask;
    }

    @Override
    public Optional<TrackHash> create(TrackHash hash) {
        if(hash != null){
            index.computeIfAbsent(hash.getHash() & mask, l -> new ArrayList()).add(hash);
        }
        return Optional.ofNullable(hash);
    }

    @Override
    public List<TrackHash> findByHash(long hash) {
        return index.getOrDefault(hash & mask, Collections.emptyList());
    }
}
