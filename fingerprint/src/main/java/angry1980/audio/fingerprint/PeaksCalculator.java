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

public class PeaksCalculator implements Calculator<Fingerprint>{

    private static Logger LOG = LoggerFactory.getLogger(PeaksCalculator.class);

    private static final int FUZ_FACTOR = 2;

    //Rhythm frequencies, where the lower and upper bass notes lie.
    public static final Ranges ranges = new Ranges(40, 300, 4);

    private Adapter adapter;
    private SpectrumBuilder spectrumBuilder;

    public PeaksCalculator(Adapter adapter) {
        this.adapter = adapter;
        this.spectrumBuilder = SpectrumBuilder.create();//.windowSize(1024).overlap(512);
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
                    .map(spectrum -> this.calculateHashes(track, spectrum))
                    .map(hashes -> ImmutableFingerprint.builder()
                                    .trackId(track.getId())
                                    .hashes(hashes)
                                    .type(FingerprintType.PEAKS)
                                        .build()
                    )
        ;
    }

    private List<TrackHash> calculateHashes(Track track, Stream<Numbered<double[]>> spectrum){
        LOG.debug("Start of hashes calculation for track {}" , track.getId());
        return spectrum
                .map(numbered -> createTrackHash(track.getId(), numbered.getNumberAsInt(), numbered.getValue()))
                .collect(Collectors.toList());
    }

    private TrackHash createTrackHash(long trackId, int time, double[] window){
        return ImmutableTrackHash.builder()
                .trackId(trackId)
                .time(time)
                .hash(hash(window))
                .build();
    }

    private long hash(double[] data){
        int[] points = choosePeaks(data);
        //LOG.debug(Arrays.toString(points));
        return (points[3] - (points[3] % FUZ_FACTOR)) * 100000000
                + (points[2] - (points[2] % FUZ_FACTOR))* 100000
                + (points[1] - (points[1] % FUZ_FACTOR)) * 100
                + (points[0] - (points[0] % FUZ_FACTOR));
    }

    private int[] choosePeaks(double[] data){
        int windowSize = data.length/2;
        int[] points = new int[ranges.getRangesCount()];
        int rangeNumber = 0;
        int rangeLimit = ranges.getRangeLimit(rangeNumber + 1);
        double max = 0;
        points[rangeNumber] = ranges.getLowerLimit();
        for(int freq = ranges.getLowerLimit(); freq < ranges.getUpperLimit(); freq++){
            if(freq >= rangeLimit){
                rangeNumber++;
                rangeLimit = ranges.getRangeLimit(rangeNumber + 1);
                max = 0;
                points[rangeNumber] = freq;
            }
            //Math.hypot(data[freq], data[freq + 1]);
            double v = Math.log(Math.hypot(data[freq], data[freq + windowSize]) + 1);
            if(max < v){
                max = v;
                points[rangeNumber] = freq;
            }
        }
        return points;
    }
}
