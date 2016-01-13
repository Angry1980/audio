package angry1980.audio.utils;

import org.tritonus.sampled.convert.PCM2PCMConversionProvider;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class AudioUtils {

    private AudioUtils(){
    }

    public static AudioFormat PCM_SIGNED_FORMAT = new AudioFormat(
            44100, //sampleRate
            8, //sampleSizeInBits
            1, //channels (mono)
            true, //signed
            true //bigEndian
    );

    public static Optional<AudioInputStream> createAudioInputStream(InputStream is){
        try {
            return Optional.of(AudioSystem.getAudioInputStream(is));
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public static Optional<byte[]> createByteArray(AudioInputStream in) {
        try(ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int len = in.read(buffer, 0, 1024);
            while (len != -1) {
                out.write(buffer, 0, len);
                len = in.read(buffer, 0, 1024);
            }
            return Optional.of(out.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public static Optional<AudioInputStream> convertToPCM_SIGNED(AudioInputStream in){
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
            System.out.println("Conversion is not supported");
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
