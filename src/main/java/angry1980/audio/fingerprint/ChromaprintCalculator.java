package angry1980.audio.fingerprint;

import angry1980.audio.model.FingerprintType;
import angry1980.audio.model.HashFingerprint;
import angry1980.audio.model.Track;
import angry1980.utils.ProcessWaiter;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ChromaprintCalculator implements Calculator<HashFingerprint>{

    @Override
    public Optional<HashFingerprint> calculate(Track track) {
        return Optional.of(new HashFingerprint(track.getId(), convertToInt(calculateAudioHash(track.getPath())), FingerprintType.CHROMAPRINT));
    }

    private int[] convertToInt(byte[] data){
        int[] hashes = new int[data.length/4];
        IntBuffer buff = ByteBuffer.wrap(data).asIntBuffer();
        for (int i = 0; i < hashes.length; i ++){
            hashes[i] = buff.get();
        }
        return hashes;
    }

    private byte[] calculateAudioHash(String file) {
        byte[] hashBuffer = null;
        try {
            List<String> params = new ArrayList<>();
            params.add("fpcalc");
            params.add(file);
            params.add("-length");
            params.add("1024");

            Process hasher = new ProcessBuilder().command(params).directory(new File("C:\\utils\\chromaprint")).start();

            ProcessWaiter.Result hasherResult = ProcessWaiter.waitFor(hasher, 5000);

            if (hasherResult.isTimeout()) {
            } else if (hasherResult.getCode() != 0) {
            } else {
                hashBuffer = hasherResult.getOutputStream().toByteArray();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return hashBuffer;
    }
    /*

    private static final int INT_LEN = 4;

    private static final boolean IS_LITTLE_ENDIAN_NATIVE = ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN);

    private int readInt(byte[] hasherOutput) throws IOException {

        if (hasherOutput.length < INT_LEN) {
            throw new EOFException("Hasher output is less than 4 bytes");
        }

        int b1 = hasherOutput[0] & 0xFF;
        int b2 = hasherOutput[1] & 0xFF;
        int b3 = hasherOutput[2] & 0xFF;
        int b4 = hasherOutput[3] & 0xFF;

        return IS_LITTLE_ENDIAN_NATIVE
                ? (b4 << 24) | (b3 << 16) | (b2 << 8) | b1
                : (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
    }

    private byte[] readAudioHash(byte[] hasherOutput) throws IOException {
        int len = readInt(hasherOutput) * INT_LEN;

        int dataLen = hasherOutput.length - INT_LEN;
        if (len < 0 || len != dataLen) {
            throw new IOException("Incorrect length of hash: " + len + " for hash array of length " + dataLen);
        }

        byte[] hashBytes = new byte[dataLen];
        if (IS_LITTLE_ENDIAN_NATIVE) {
            for (int i = 0, j = INT_LEN; i < dataLen; i += INT_LEN, j += INT_LEN) {
                hashBytes[i]     = hasherOutput[j + 3];
                hashBytes[i + 1] = hasherOutput[j + 2];
                hashBytes[i + 2] = hasherOutput[j + 1];
                hashBytes[i + 3] = hasherOutput[j];
            }
        } else {
            System.arraycopy(hasherOutput, INT_LEN, hashBytes, 0, dataLen);
        }

        return hashBytes;
    }
*/

}
