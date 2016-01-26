package angry1980.audio.fingerprint;

import angry1980.audio.model.*;
import angry1980.audio.utils.Complex;
import angry1980.audio.utils.SpectrumBuilder;
import angry1980.audio.Adapter;
import angry1980.utils.Numbered;
import angry1980.utils.Ranges;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * ported from https://github.com/wsieroci/audiorecognizer
 */
public class PeaksCalculator implements Calculator<PeaksFingerprint>{

    private static final int FUZ_FACTOR = 2;

    //Rhythm frequencies, where the lower and upper bass notes lie.
    public static final Ranges ranges = new Ranges(40, 300, 4);

    private SpectrumBuilder builder;

    public PeaksCalculator(Adapter adapter) {
        this.builder = SpectrumBuilder.create(Objects.requireNonNull(adapter));
    }

    @Override
    public Optional<PeaksFingerprint> calculate(Track track) {
        return builder.build(track)
                .map(this::determineKeyPoints)
                .map(points -> build(track.getId(), points))
        ;
    }
    private PeaksFingerprint build(long trackId, List<Peak> peaks){
        return ImmutablePeaksFingerprint.builder()
                .trackId(trackId)
                .points(peaks)
                .build();
    }


    private List<Peak> determineKeyPoints(Spectrum spectrum) {
        return IntStream.range(0, spectrum.getData().length)
                .mapToObj(t -> new Numbered<>(t, hash(spectrum.getData()[t])))
                .map(t -> build(spectrum.getTrackId(), t.getNumberAsInt(), t.getValue()))
                .collect(Collectors.toList());
    }

    private Peak build(long trackId, int time, long hash){
        return ImmutablePeak.builder()
                .trackId(trackId)
                .time(time)
                .hash(hash)
                .build();
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
