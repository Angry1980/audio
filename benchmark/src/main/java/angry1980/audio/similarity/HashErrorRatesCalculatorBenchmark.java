package angry1980.audio.similarity;

import angry1980.audio.dao.FingerprintDAO;
import angry1980.audio.dao.FingerprintDAOInMemoryImpl;
import angry1980.audio.model.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Collection;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@State(Scope.Benchmark)
public class HashErrorRatesCalculatorBenchmark {

    private static Random RND = new Random();

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(".*" + HashErrorRatesCalculatorBenchmark.class.getSimpleName() + ".*")
                .jvmArgs("-server")
                .warmupIterations(5)
                .forks(1)
                .measurementIterations(5)
                .build();
        new Runner(opt).run();
    }

    @Param({"100", "1000", "10000"})
    private int tracksCount;
    @Param("8")
    private int errorBitLimit;
    @Param("25")
    private int batchSize;
    @Param({"1", "2"})
    private long track;

    private Collection<Track> tracks;
    private Fingerprint fingerprint;
    private HashErrorRatesCalculator calculator;

    @Setup
    public void init(){
        FingerprintDAO<Fingerprint> fingerprintDAO = new FingerprintDAOInMemoryImpl<>();
        tracks = IntStream.range(0, tracksCount)
                        .mapToObj(trackId -> ImmutableTrack.builder().id(trackId).cluster(1).path("").build())
                        .collect(Collectors.toList());
        tracks.stream()
                .map(track -> ImmutableFingerprint.builder()
                                .trackId(track.getId())
                                .type(FingerprintType.CHROMAPRINT)
                                .hashes(IntStream.range(0, 2000)
                                            .mapToObj(time -> ImmutableTrackHash.builder()
                                                                .hash(RND.nextLong())
                                                                .time(time)
                                                                .trackId(track.getId())
                                                                .build()
                                            ).collect(Collectors.toList())
                                ).build())
                .forEach(fingerprintDAO::create);
        fingerprint = fingerprintDAO.findByTrackId(track).get();
        calculator = new HashErrorRatesCalculator(
                            FingerprintType.CHROMAPRINT,
                            new TrackSource(),
                            fingerprintDAO,
                            batchSize,
                            errorBitLimit
        );
    }

    @Benchmark
    public Collection<TrackSimilarity> testCalculate(){
        return calculator.calculate(fingerprint);
    }

    public class TrackSource implements HashErrorRatesCalculatorTrackSource{

        @Override
        public Optional<Collection<Track>> get(long sourceTrackId) {
            return Optional.of(tracks);
        }
    }

}
