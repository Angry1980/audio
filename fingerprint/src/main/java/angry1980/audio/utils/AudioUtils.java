package angry1980.audio.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tritonus.sampled.convert.PCM2PCMConversionProvider;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class AudioUtils {

    private static Logger LOG = LoggerFactory.getLogger(AudioUtils.class);

    private AudioUtils(){
    }

    public static AudioFormat PCM_SIGNED_FORMAT = new AudioFormat(
            44100, //sampleRate
            8, //sampleSizeInBits
            1, //channels (mono)
            true, //signed
            true //bigEndian
    );

    public static Optional<AudioInputStream> createAudioInputStream(File file){
        LOG.debug("Trying to create audio input stream from {}", file.getAbsolutePath());
        try{
            return Optional.of(AudioSystem.getAudioInputStream(file));
        } catch (Exception e) {
            LOG.error("Error while trying to create audio input stream", e);
        }
        return Optional.empty();
    }

    public static Optional<byte[]> createByteArray(AudioInputStream in) {
        LOG.debug("Trying to create byte array from audio input stream");
        try(ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int len = in.read(buffer, 0, 1024);
            while (len != -1) {
                out.write(buffer, 0, len);
                len = in.read(buffer, 0, 1024);
            }
            return Optional.of(out.toByteArray());
        } catch (IOException e) {
            LOG.error("Error while reading audio content", e);
        }
        return Optional.empty();
    }

    public static Optional<AudioInputStream> convertToPCM_SIGNED(AudioInputStream in, boolean doIt){
        if(!doIt){
            return Optional.of(in);
        }
        LOG.debug("Trying to convert audio input stream to PCM signed format");
        PCM2PCMConversionProvider conversionProvider = new PCM2PCMConversionProvider();
        AudioFormat baseFormat = in.getFormat();
        AudioFormat decodedFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.getSampleRate(),
                16,
                baseFormat.getChannels(),
                baseFormat.getChannels() * 2,
                baseFormat.getSampleRate(),
                false
        );
        if (!conversionProvider.isConversionSupported(PCM_SIGNED_FORMAT, decodedFormat)) {
            LOG.warn("Conversion to PCM signed format is not supported");
            return Optional.empty();
        }
        return Optional.ofNullable(
                conversionProvider.getAudioInputStream(
                    PCM_SIGNED_FORMAT,
                    AudioSystem.getAudioInputStream(decodedFormat, in)
                )
        );
    }
}
