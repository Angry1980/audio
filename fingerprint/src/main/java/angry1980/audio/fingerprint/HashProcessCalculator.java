package angry1980.audio.fingerprint;

import angry1980.audio.Adapter;
import angry1980.audio.model.FingerprintType;
import angry1980.audio.model.HashFingerprint;
import angry1980.audio.model.Track;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Objects;

public class HashProcessCalculator extends ProcessCalculator<HashFingerprint>{

    private FingerprintType type;

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
        return new HashFingerprint(track.getId(), convertToInt(hash), type);
    }

}
