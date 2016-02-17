package angry1980.audio.dao;

import angry1980.audio.model.TrackHash;
import it.unimi.dsi.fastutil.longs.*;

import java.util.*;
import java.util.stream.Collectors;

public class TrackHashDAOInMemoryImpl implements TrackHashDAO {

    private static Comparator<TrackHash> c = Comparator.comparingInt(TrackHash::getTime);

    private final long mask;
    private final Long2ObjectMap<Collection<TrackHash>> index;

    public TrackHashDAOInMemoryImpl(){
        this(16, -1);
    }

    public TrackHashDAOInMemoryImpl(long mask){
        this(16, mask);
    }

    public TrackHashDAOInMemoryImpl(int expectedSize, long mask){
        this.index = new Long2ObjectOpenHashMap<>(expectedSize);
        this.mask = mask;
    }

    @Override
    public Optional<TrackHash> create(TrackHash hash) {
        if(hash != null){
            index.computeIfAbsent(getKey(hash.getHash()), l -> new ArrayList<>()).add(hash);
        }
        return Optional.ofNullable(hash);
    }

    @Override
    public Collection<TrackHash> findByHash(long hash) {
        return index.getOrDefault(getKey(hash), Collections.emptyList());
    }

    @Override
    public Collection<TrackHash> findByHashes(long[] hashes) {
        return Arrays.stream(hashes)
                .mapToObj(this::findByHash)
                .flatMap(data -> data.stream())
                .collect(Collectors.toSet());
    }

    private long getKey(long hash){
        if(mask == -1){
            return hash;
        }
        return hash & mask;
    }
}
