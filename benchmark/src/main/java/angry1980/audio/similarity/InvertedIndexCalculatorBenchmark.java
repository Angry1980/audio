package angry1980.audio.similarity;

import angry1980.audio.fingerprint.HashInvertedIndex;
import angry1980.audio.model.*;
import angry1980.audio.dao.TrackHashDAOState;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@State(Scope.Benchmark)
public class InvertedIndexCalculatorBenchmark {

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(".*" + InvertedIndexCalculatorBenchmark.class.getSimpleName() + ".*")
                .jvmArgs("-server")
                .warmupIterations(5)
                .forks(1)
                .measurementIterations(5)
                .build();
        new Runner(opt).run();
    }

    @Param({"1", "2", "3", "4"})
    public int track;

    private Fingerprint fingerprint;
    private InvertedIndexCalculator index;

    @Setup
    public void init(TrackHashDAOState daoState){
        index = new InvertedIndexCalculator(0.01, 0.05, new HashInvertedIndex(daoState.dao, Optional.empty()));
        Collection<TrackHash> hashes = IntStream.range(0, daoState.hashes[track].length)
                .mapToObj(time -> ImmutableTrackHash.builder()
                        .trackId(track)
                        .time(time)
                        .hash(daoState.hashes[track][time])
                        .build()
                ).collect(Collectors.toList());
        fingerprint = ImmutableFingerprint.builder()
                .trackId(track)
                .type(FingerprintType.CHROMAPRINT)
                .hashes(hashes)
                .build();
    }

    @Benchmark
    public Collection<TrackSimilarity> testCalculate(){
        return index.calculate(fingerprint, ComparingType.CHROMAPRINT).toList().toBlocking().single();
    }

}
