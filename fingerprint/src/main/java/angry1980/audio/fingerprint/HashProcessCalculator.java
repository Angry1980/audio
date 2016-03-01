package angry1980.audio.fingerprint;

import angry1980.audio.Adapter;
import angry1980.audio.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HashProcessCalculator extends ProcessCalculator<Fingerprint>{

    private static Logger LOG = LoggerFactory.getLogger(HashProcessCalculator.class);

    private final ComparingType type;

    public HashProcessCalculator(ProcessCreator creator, Adapter adapter, ComparingType type) {
        super(creator, adapter);
        this.type = Objects.requireNonNull(type);
    }

    private List<TrackHash> convert(long trackId, byte[] data){
        int size = data.length/4;
        List<TrackHash> hashes = new ArrayList<>(size);
        IntBuffer buff = ByteBuffer.wrap(data).asIntBuffer();
        for (int i = 0; i < size; i ++){
            hashes.add(ImmutableTrackHash.builder().hash(buff.get()).time(i).trackId(trackId).build());
        }
        return hashes;
    }

    @Override
    protected Fingerprint create(Track track, byte[] hash) {
        LOG.debug("Creation of fingerprint entity for track {}", track.getId());
        Fingerprint f = ImmutableFingerprint.builder()
                .trackId(track.getId())
                .hashes(convert(track.getId(), hash))
                .type(type)
                .build();
        LOG.debug("Fingerprint was created for track {}", track.getId());
        LOG.debug("There are {} hash values in fingerprint for track {} ", f.getHashes().size(), track.getId());
        return f;
    }

}
