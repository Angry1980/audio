package angry1980.audio.utils;

import angry1980.utils.Numbered;
import org.jtransforms.fft.DoubleFFT_1D;

import javax.sound.sampled.AudioInputStream;
import java.io.File;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SpectrumBuilder {

    public static SpectrumBuilder create(){
        return new SpectrumBuilder().overlap(0)
                        .windowSize(4096)
                        .maxWidth(0)
                        .convertToPCM_SIGNED(false)
        ;
    }

    private int overlap;
    private int windowSize;
    private int maxWidth;
    private boolean convertToPCM_SIGNED;

    private SpectrumBuilder(){}

    public SpectrumBuilder overlap(int overlap) {
        this.overlap = overlap;
        return this;
    }

    public SpectrumBuilder windowSize(int windowSize) {
        this.windowSize = windowSize;
        return this;
    }

    public SpectrumBuilder maxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }

    public SpectrumBuilder convertToPCM_SIGNED(boolean convertToPCM_SIGNED) {
        this.convertToPCM_SIGNED = convertToPCM_SIGNED;
        return this;
    }

    public Optional<Stream<Numbered<double[]>>> build(File track){
        return Optional.of(track)
                .flatMap(AudioUtils::createAudioInputStream)
                //todo: check
                .flatMap(in -> AudioUtils.convertToPCM_SIGNED(in, convertToPCM_SIGNED))
                .flatMap(this::build)
        ;
    }

    public Optional<Stream<Numbered<double[]>>> build(AudioInputStream in){
        return Optional.of(in)
                .flatMap(AudioUtils::createByteArray)
                .map(bytes -> new Calculator(overlap, windowSize, maxWidth).calculate(bytes))
        ;
    }

    private static class Calculator{

        private final DoubleFFT_1D fft;
        private final int overlap;
        private final int windowSize;
        private final int maxWidth;


        public Calculator(int overlap, int windowSize, int maxWidth) {
            this.windowSize = windowSize;
            this.overlap = overlap > 0 ? overlap : windowSize;
            this.maxWidth = maxWidth > 0 ? maxWidth : Integer.MAX_VALUE;
            this.fft = new DoubleFFT_1D(this.windowSize);
        }

        public Stream<Numbered<double[]>> calculate(byte[] audio){
            int amountPossible = Math.min(maxWidth, ((audio.length - windowSize) / overlap)); //width of the image
            return IntStream.range(0, amountPossible)
                    .mapToObj(times -> new Numbered<>(times, getWindow(audio, times, overlap)))
                    //fft.realForward is faster than fft.complexForward and gives correct results
                    .peek(window -> fft.realForward(window.getValue()))
                    //.peek(window -> fft.complexForward(window.getValue()))
                    ;
        }

        private double[] getWindow(byte[] audio, int times, int overlap){
            int size = Math.min(windowSize, audio.length - times * overlap);
            double[] data = new double[size * 2];
            IntStream.range(0, size)
                    .forEach(i -> data[i] = audio[(times * overlap) + i]);
            // complex
            //.forEach(i -> data[2*i] = audio[(times * overlap) + i]);
            return data;
        }

    }
}
