package angry1980.audio.fingerprint;

import angry1980.audio.model.*;
import angry1980.audio.utils.AudioUtils;
import angry1980.audio.Adapter;
import angry1980.utils.Numbered;
import angry1980.utils.Ranges;
import org.jtransforms.fft.DoubleFFT_1D;
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
                .map(Numbered.<double[], Long>transformator(this::hash))
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

    private Stream<Numbered<double[]>> calculateSpectrum(byte[] audio){
        DoubleFFT_1D fft = new DoubleFFT_1D(windowSize);
        final int overlap = this.overlap > 0 ? this.overlap : this.windowSize;
        int maxWidth = this.maxWidth > 0 ? this.maxWidth : Integer.MAX_VALUE;
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

    private long hash(double[] data){
        Map<Integer, Long> points = ranges.stream()
                // Get the magnitude:
                .mapToObj(freq -> new Numbered<>(freq, Math.log(abs(data, freq) + 1)))
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

    private double abs(double[] data, int freq) {
        return Math.hypot(data[freq], data[freq + windowSize]);
        // complex
        //return Math.hypot(data[freq], data[freq + 1]);
    }

}
