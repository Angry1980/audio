package angry1980.audio.fingerprint;

import angry1980.audio.dao.ChromaprintHashDAO;
import angry1980.audio.model.ChromaprintFingerprint;
import angry1980.audio.model.ChromaprintHash;
import angry1980.audio.model.TrackSimilarity;
import angry1980.audio.similarity.Calculator;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ChromaprintInvertedIndex implements InvertedIndex<ChromaprintFingerprint>, Calculator<ChromaprintFingerprint> {

    private ChromaprintHashDAO hashDAO;

    public ChromaprintInvertedIndex(ChromaprintHashDAO hashDAO) {
        this.hashDAO = Objects.requireNonNull(hashDAO);
    }

    @Override
    public ChromaprintFingerprint save(ChromaprintFingerprint fingerprint) {
        Arrays.stream(fingerprint.getHashes())
                .mapToObj(hash -> new ChromaprintHash(fingerprint.getTrackId(), hash))
                .forEach(hashDAO::create)
        ;
        return fingerprint;
    }

    @Override
    public List<TrackSimilarity> calculate(ChromaprintFingerprint fingerprint) {
        throw new UnsupportedOperationException();
    }

}
