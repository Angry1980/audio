package angry1980.audio.fingerprint;

import angry1980.audio.ClassPathAdapter;
import angry1980.audio.model.Fingerprint;
import angry1980.audio.model.ImmutableTrack;
import angry1980.audio.model.Track;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Optional;

@State(Scope.Benchmark)
public class PeaksCalculatorBenchmark {

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(".*" + PeaksCalculatorBenchmark.class.getSimpleName() + ".*")
                .jvmArgs("-server")
                .warmupIterations(5)
                .forks(1)
                .measurementIterations(5)
                .build();
        new Runner(opt).run();
    }

    @Param({"/test1.mp3","/test1.mp3"})
    public String trackPath;

    private Track track;
    private PeaksCalculator calculator;

    @Setup
    public void init(){
        track = ImmutableTrack.builder().cluster(0).id(0).path(trackPath).build();
        calculator = new PeaksCalculator(new ClassPathAdapter());
    }

    @Benchmark
    public Optional<Fingerprint> testCalculate(){
        return calculator.calculate(track);
    }

}
