package angry1980.audio.utils;

import angry1980.audio.Adapter;
import angry1980.audio.model.Spectrum;
import angry1980.audio.model.Track;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SpectrumBuilder {


    public static SpectrumBuilder create(Adapter adapter){
        return new SpectrumBuilder(adapter);
    }

    private Adapter adapter;

    public SpectrumBuilder(Adapter adapter) {
        this.adapter = Objects.requireNonNull(adapter);
    }

    public Optional<Spectrum> build(Track track) {
        return Optional.of(track)
                .flatMap(t -> adapter.getContent(t.getPath()))
                .flatMap(AudioUtils::createAudioInputStream)
                .flatMap(AudioUtils::convertToPCM_SIGNED)
                .flatMap(AudioUtils::createByteArray)
                .map(this::calculateSpectrum)
                .map(c -> new Spectrum(track.getId(), c))
                ;
    }

    private Complex[][] calculateSpectrum(byte[] audio) {
        int amountPossible = audio.length / 4096;
        return IntStream.range(0, amountPossible)
                .mapToObj(times -> IntStream.range(0, 4096)
                                    .mapToObj(i -> new Complex(audio[(times * 4096) + i], 0))
                                    .collect(Collectors.toList())
                ).map(FFT::fft)
                .collect(Collectors.toList())
                .toArray(new Complex[amountPossible][])
        ;
    }


}
