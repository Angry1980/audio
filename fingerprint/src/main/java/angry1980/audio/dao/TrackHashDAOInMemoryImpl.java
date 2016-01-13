package angry1980.audio.dao;

import angry1980.audio.model.TrackHash;

import java.util.*;

public class TrackHashDAOInMemoryImpl implements TrackHashDAO {

    private Map<Integer, List<TrackHash>> index;

    public TrackHashDAOInMemoryImpl(){
        this.index = new HashMap<>();
    }

    @Override
    public Optional<TrackHash> create(TrackHash hash) {
        index.computeIfAbsent(hash.getHash(), l -> new ArrayList()).add(hash);
        return Optional.of(hash);
    }

    @Override
    public List<TrackHash> findByHash(int hash) {
        return index.getOrDefault(hash, Collections.emptyList());
    }
}
