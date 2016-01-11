package angry1980.audio.dao;

import angry1980.audio.model.Peak;

import java.util.*;

public class PeakDAOInMemoryImpl implements PeakDAO {

    private Map<Long, List<Peak>> index;

    public PeakDAOInMemoryImpl() {
        this.index = new HashMap<>();
    }

    @Override
    public List<Peak> findByHash(long hash) {
        return index.getOrDefault(hash, Collections.emptyList());
    }

    @Override
    public Optional<Peak> create(Peak point) {
        index.computeIfAbsent(point.getHash(), v -> new ArrayList<>()).add(point);
        return Optional.of(point);
    }
}
