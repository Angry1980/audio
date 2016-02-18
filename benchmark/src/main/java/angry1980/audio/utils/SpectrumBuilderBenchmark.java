package angry1980.audio.utils;

import angry1980.utils.Numbered;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.stream.Stream;

@State(Scope.Benchmark)
public class SpectrumBuilderBenchmark {

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(".*" + SpectrumBuilderBenchmark.class.getSimpleName() + ".*")
                .jvmArgs("-server")
                .warmupIterations(5)
                .forks(1)
                .measurementIterations(5)
                .build();
        new Runner(opt).run();
    }

    @Param({"/test1.mp3"})
    public String filePath;
    @Param({"1024", "2048", "4096", "8192"})
    public int windowSize;
    @Param({"0", "512", "1024"})
    public int overlap;

    private File file;
    private SpectrumBuilder spectrumBuilder;

    @Setup
    public void init() throws URISyntaxException {
        file = new File(this.getClass().getResource(filePath).toURI());
        spectrumBuilder = SpectrumBuilder.create().overlap(overlap).windowSize(windowSize);
    }

    @Benchmark
    public Optional<Stream<Numbered<double[]>>> testBuild(){
        return spectrumBuilder.build(file);
    }
}
