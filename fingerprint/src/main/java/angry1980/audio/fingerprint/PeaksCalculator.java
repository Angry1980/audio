package angry1980.audio.fingerprint;

import angry1980.audio.model.*;
import angry1980.audio.utils.AudioUtils;
import angry1980.audio.utils.Complex;
import angry1980.audio.utils.FFT;
import angry1980.audio.Adapter;
import angry1980.utils.Numbered;
import angry1980.utils.Ranges;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * ported from https://github.com/wsieroci/audiorecognizer
 */
public class PeaksCalculator implements Calculator<Fingerprint>{

    private static Logger LOG = LoggerFactory.getLogger(PeaksCalculator.class);

    private static final int FUZ_FACTOR = 2;

    //Rhythm frequencies, where the lower and upper bass notes lie.
    public static final Ranges ranges = new Ranges(40, 300, 4);

    private Adapter adapter;
    private int overlap = 0;
    private int windowSize = 4096;
    private int maxWidth = 0;
    private boolean convertToPCM_SIGNED = false;

    public PeaksCalculator(Adapter adapter) {
        this.adapter = adapter;
    }

    public PeaksCalculator setConvertToPCM_SIGNED(boolean convertToPCM_SIGNED) {
        this.convertToPCM_SIGNED = convertToPCM_SIGNED;
        return this;
    }

    @Override
    public Optional<Fingerprint> calculate(Track track) {
        LOG.debug("Start of peaks fingerprint calculation for track {}", track.getId());
        return Optional.of(track)
                    .flatMap(adapter::getContent)
                    .flatMap(AudioUtils::createAudioInputStream)
                    //todo: check
                    .flatMap(in -> AudioUtils.convertToPCM_SIGNED(in, convertToPCM_SIGNED))
                    .flatMap(AudioUtils::createByteArray)
                    .map(audio -> this.calculateHashes(track, audio))
                    .map(list -> createFingerprint(track.getId(), list))
        ;
    }

    private Fingerprint createFingerprint(long trackId, List<TrackHash> peaks){
        return ImmutableFingerprint.builder()
                .trackId(trackId)
                .hashes(peaks)
                .type(FingerprintType.PEAKS)
                .build();
    }

    private List<TrackHash> calculateHashes(Track track, byte[] audio){
        LOG.debug("Start of hashes calculation for track {}" , track.getId());
        return calculateSpectrum(audio)
                .map(Numbered.<Complex[], Long>transformator(this::hash))
                .map(numbered -> createTrackHash(track.getId(), numbered.getNumberAsInt(), numbered.getValue()))
                .collect(Collectors.toList());
    }

    private TrackHash createTrackHash(long trackId, int time, long hash){
        return ImmutableTrackHash.builder()
                .trackId(trackId)
                .time(time)
                .hash(hash)
                .build();
    }

    private Stream<Numbered<Complex[]>> calculateSpectrum(byte[] audio){
        final int overlap = this.overlap > 0 ? this.overlap : this.windowSize;
        int maxWidth = this.maxWidth > 0 ? this.maxWidth : Integer.MAX_VALUE;
        int amountPossible = Math.min(maxWidth, ((audio.length - windowSize) / overlap)); //width of the image
        return IntStream.range(0, amountPossible)
                .mapToObj(times -> new Numbered<>(times, calculateWindow(audio, times, overlap)))
                .map(Numbered.<Complex[], Complex[]>transformator(FFT::fft))
        ;
    }

    private Complex[] calculateWindow(byte[] audio, int times, int overlap){
        return IntStream.range(0, windowSize)
                .mapToObj(i -> new Complex(audio[(times * overlap) + i], 0))
                .toArray(Complex[]::new);
    }

    private long hash(Complex[] data){
        Map<Integer, Long> points = ranges.stream()
                // Get the magnitude:
                .mapToObj(freq -> new Numbered<>(freq, Math.log(data[freq].abs() + 1)))
                .collect(
                        Collectors.groupingBy(numbered -> ranges.getIndex(numbered.getNumberAsInt()),
                                Collectors.collectingAndThen(
                                        Collectors.maxBy((n1, n2) -> Double.compare(n1.getValue(), n2.getValue())),
                                        o -> o.map(numbered -> numbered.getNumber()).orElse(0L)
                                )
                        )
                );
        return (points.get(3) - (points.get(3) % FUZ_FACTOR)) * 100000000
                + (points.get(2) - (points.get(2) % FUZ_FACTOR))* 100000
                + (points.get(1) - (points.get(1) % FUZ_FACTOR)) * 100
                + (points.get(0) - (points.get(0) % FUZ_FACTOR));
    }

}
