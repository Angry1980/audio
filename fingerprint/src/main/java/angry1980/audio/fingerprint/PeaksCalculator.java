package angry1980.audio.fingerprint;

import angry1980.audio.model.Peak;
import angry1980.audio.model.PeaksFingerprint;
import angry1980.audio.model.Spectrum;
import angry1980.audio.model.Track;
import angry1980.audio.utils.Complex;
import angry1980.audio.utils.SpectrumBuilder;
import angry1980.audio.Adapter;
import angry1980.utils.Numbered;
import angry1980.utils.Ranges;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * ported from https://github.com/wsieroci/audiorecognizer
 */
public class PeaksCalculator implements Calculator<PeaksFingerprint>{

    private static Logger LOG = LoggerFactory.getLogger(PeaksCalculator.class);

    private static final int FUZ_FACTOR = 2;

    //Rhythm frequencies, where the lower and upper bass notes lie.
    public static final Ranges ranges = new Ranges(40, 300, 4);

    private SpectrumBuilder builder;

    public PeaksCalculator(Adapter adapter) {
        this.builder = SpectrumBuilder.create(Objects.requireNonNull(adapter));
    }

    @Override
    public Optional<PeaksFingerprint> calculate(Track track) {
        LOG.debug("Start of peaks fingerprint calculation for track {}", track.getId());
        return builder.build(track)
                .map(this::determineKeyPoints)
                .map(points -> new PeaksFingerprint(track.getId(), points))
        ;
    }

    private List<Peak> determineKeyPoints(Spectrum spectrum) {
        LOG.debug("Spectrum key points determination for track {}", spectrum.getTrackId());
        return IntStream.range(0, spectrum.getData().length)
                .mapToObj(t -> new Numbered<>(t, hash(spectrum.getData()[t])))
                .map(t -> new Peak(spectrum.getTrackId(), t.getNumberAsInt(), t.getValue()))
                .collect(Collectors.toList());
    }

    private long hash(Complex[] data){
        return hash(ranges.stream()
                    // Get the magnitude:
                    .mapToObj(freq -> new Numbered<>(freq, Math.log(data[freq].abs() + 1)))
                    .collect(
                        Collectors.groupingBy(tuple -> ranges.getIndex(tuple.getNumberAsInt()),
                                Collectors.collectingAndThen(
                                        Collectors.maxBy((tuple1, tuple2) -> Double.compare(tuple1.getValue(), tuple2.getValue())),
                                        o -> o.map(tuple -> tuple.getNumber()).orElse(0L)
                                )
                        )
                    ).values()
        );
    }

    private long hash(Collection<Long> data) {
        List<Long> points = new ArrayList<>(data);
        return (points.get(3) - (points.get(3) % FUZ_FACTOR)) * 100000000
                + (points.get(2) - (points.get(2) % FUZ_FACTOR))* 100000
                + (points.get(1) - (points.get(1) % FUZ_FACTOR)) * 100
                + (points.get(0) - (points.get(0) % FUZ_FACTOR));
    }



}
