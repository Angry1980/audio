package angry1980.audio.dao;

import angry1980.audio.model.ChromaprintHash;

import java.util.Optional;

public interface ChromaprintHashDAO {

    Optional<ChromaprintHash> create(ChromaprintHash hash);
}
