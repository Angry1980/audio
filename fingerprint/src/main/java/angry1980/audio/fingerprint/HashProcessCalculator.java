package angry1980.audio.fingerprint;

import angry1980.audio.Adapter;
import angry1980.audio.model.FingerprintType;
import angry1980.audio.model.HashFingerprint;
import angry1980.audio.model.ImmutableHashFingerprint;
import angry1980.audio.model.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Objects;

public class HashProcessCalculator extends ProcessCalculator<HashFingerprint>{

    private static Logger LOG = LoggerFactory.getLogger(HashProcessCalculator.class);

    private final FingerprintType type;

    public HashProcessCalculator(ProcessCreator creator, Adapter adapter, FingerprintType type) {
        super(creator, adapter);
        this.type = Objects.requireNonNull(type);
    }

    private int[] convertToInt(byte[] data){
        int[] hashes = new int[data.length/4];
        IntBuffer buff = ByteBuffer.wrap(data).asIntBuffer();
        for (int i = 0; i < hashes.length; i ++){
            hashes[i] = buff.get();
        }
        return hashes;
    }

    @Override
    protected HashFingerprint create(Track track, byte[] hash) {
        LOG.debug("Creation of fingerprint entity for track {}", track.getId());
        HashFingerprint f = ImmutableHashFingerprint.builder()
                .trackId(track.getId())
                .hashes(convertToInt(hash))
                .type(type)
                .build();
        LOG.debug("{} was created for track {}", f, track.getId());
        LOG.debug("There are {} hash values in fingerprint for track {} ", f.getHashes().length, track.getId());
        return f;
    }

}
