package angry1980.audio.fingerprint;

import angry1980.audio.Adapter;
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

public class LastFMCalculator implements Calculator<HashFingerprint> {

    private Adapter adapter;

    public LastFMCalculator(Adapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public Optional<HashFingerprint> calculate(Track track) {
        return Optional.of(track)
                .flatMap(adapter::getContent)
                .map(this::calculateAudioHash)
                .map(this::convertToInt)
                .map(hash -> new HashFingerprint(track.getId(), hash, FingerprintType.LASTFM))
        ;
    }

    private int[] convertToInt(byte[] data){
        int[] hashes = new int[data.length/4];
        IntBuffer buff = ByteBuffer.wrap(data).asIntBuffer();
        for (int i = 0; i < hashes.length; i ++){
            hashes[i] = buff.get();
        }
        return hashes;
    }

    private byte[] calculateAudioHash(File file) {
        byte[] hashBuffer = null;
        try {
            List<String> params = new ArrayList<>();
            params.add("lastfm-fpclient");
            params.add(file.getAbsolutePath());

            Process hasher = new ProcessBuilder().command(params).start();//.directory(new File("C:\\utils\\chromaprint")).start();
            //hasher.waitFor(4, TimeUnit.SECONDS);
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

}
