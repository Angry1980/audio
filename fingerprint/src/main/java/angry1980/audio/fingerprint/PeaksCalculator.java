package angry1980.audio.fingerprint;

import angry1980.audio.model.*;
import angry1980.audio.Adapter;
import angry1980.audio.utils.SpectrumBuilder;
import angry1980.utils.Numbered;
import angry1980.utils.Ranges;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
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
    private SpectrumBuilder spectrumBuilder;

    public PeaksCalculator(Adapter adapter) {
        this.adapter = adapter;
        this.spectrumBuilder = SpectrumBuilder.create();
    }

    public PeaksCalculator setConvertToPCM_SIGNED(boolean convertToPCM_SIGNED) {
        spectrumBuilder.convertToPCM_SIGNED(convertToPCM_SIGNED);
        return this;
    }

    @Override
    public Optional<Fingerprint> calculate(Track track) {
        LOG.debug("Start of peaks fingerprint calculation for track {}", track.getId());
        return Optional.of(track)
                    .flatMap(adapter::getContent)
                    .flatMap(spectrumBuilder::build)
                    .map(in -> this.calculateHashes(track, in))
                    .map(peaks -> ImmutableFingerprint.builder()
                                    .trackId(track.getId())
                                    .hashes(peaks)
                                    .type(FingerprintType.PEAKS)
                                        .build()
                    )
        ;
    }

    private List<TrackHash> calculateHashes(Track track, Stream<Numbered<double[]>> spectrum){
        LOG.debug("Start of hashes calculation for track {}" , track.getId());
        return spectrum
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
        int windowSize = data.length/2;
        return Math.hypot(data[freq], data[freq + windowSize]);
        // complex
        //return Math.hypot(data[freq], data[freq + 1]);
    }

}
