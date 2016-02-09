package angry1980.audio.utils;

import angry1980.audio.Adapter;
import angry1980.audio.model.ImmutableSpectrum;
import angry1980.audio.model.Spectrum;
import angry1980.audio.model.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

public class SpectrumBuilder {

    private static Logger LOG = LoggerFactory.getLogger(SpectrumBuilder.class);

    public static SpectrumBuilder create(Adapter adapter){
        return new SpectrumBuilder(adapter);
    }

    private Adapter adapter;
    private int overlap = 0;
    private int windowSize = 4096;
    private int maxWidth = 0;

    public SpectrumBuilder(Adapter adapter) {
        this.adapter = Objects.requireNonNull(adapter);
    }

    public SpectrumBuilder overlap(int overlap) {
        if(overlap >= 0){
            this.overlap = overlap;
        }
        return this;
    }

    public SpectrumBuilder windowSize(int windowSize) {
        if(windowSize > 0 && windowSize % 2 == 0){
            this.windowSize = windowSize;
        }
        return this;
    }

    public SpectrumBuilder maxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }

    public Optional<Spectrum> build(Track track) {
        LOG.debug("Start of spectrum calculation for track {}" , track.getId());
        return Optional.of(track)
                .flatMap(adapter::getContent)
                .flatMap(AudioUtils::createAudioInputStream)
                //todo: check
                //.flatMap(AudioUtils::convertToPCM_SIGNED)
                .flatMap(AudioUtils::createByteArray)
                .map(this::calculateSpectrum)
                .map(c -> create(track.getId(), c))
                ;
    }

    private Spectrum create(long trackId, Complex[][] data){
        LOG.debug("Spectrum for {} is ready, size of data is {}, window size is {}", new Object[]{trackId, data.length, windowSize});
        return ImmutableSpectrum.builder()
                .trackId(trackId)
                .data(data)
                .build();
    }


    private Complex[][] calculateSpectrum(byte[] audio) {
        LOG.debug("Spectrum calculation for audio with length {}, window size is {}, overlap is {}", new Object[]{audio.length, windowSize, overlap});
        final int overlap = this.overlap > 0 ? this.overlap : this.windowSize;
        int maxWidth = this.maxWidth > 0 ? this.maxWidth : Integer.MAX_VALUE;
        int amountPossible = Math.min(maxWidth, ((audio.length - windowSize) / overlap)); //width of the image
        return IntStream.range(0, amountPossible)
                .mapToObj(times -> IntStream.range(0, windowSize)
                                    .mapToObj(i -> new Complex(audio[(times * overlap) + i], 0))
                                    .toArray(Complex[]::new)
                ).map(FFT::fft)
                .toArray(Complex[][]::new)
        ;
    }

}
