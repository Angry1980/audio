package angry1980.audio.dao;

import angry1980.audio.model.Peak;

import java.util.List;
import java.util.Optional;

public interface PeakDAO {

    List<Peak> findByHash(long hash);

    Optional<Peak> create(Peak point);
}
