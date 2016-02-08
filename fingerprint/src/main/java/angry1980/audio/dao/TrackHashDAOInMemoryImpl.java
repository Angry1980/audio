package angry1980.audio.dao;

import angry1980.audio.model.TrackHash;

import java.util.*;

public class TrackHashDAOInMemoryImpl implements TrackHashDAO {

    private int mask;
    private Map<Integer, List<TrackHash>> index;

    public TrackHashDAOInMemoryImpl(){
        this(-1);
    }

    public TrackHashDAOInMemoryImpl(int mask){
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
    public List<TrackHash> findByHash(int hash) {
        return index.getOrDefault(hash & mask, Collections.emptyList());
    }
}
