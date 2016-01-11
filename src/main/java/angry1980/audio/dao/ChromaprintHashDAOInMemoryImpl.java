package angry1980.audio.dao;

import angry1980.audio.model.ChromaprintHash;

import java.util.*;

public class ChromaprintHashDAOInMemoryImpl implements ChromaprintHashDAO{

    private Map<Integer, List<Long>> index;

    public ChromaprintHashDAOInMemoryImpl(){
        this.index = new HashMap<>();
    }

    @Override
    public Optional<ChromaprintHash> create(ChromaprintHash hash) {
        index.computeIfAbsent(hash.getHash(), l -> new ArrayList()).add(hash.getTrackId());
        return Optional.of(hash);
    }
}
