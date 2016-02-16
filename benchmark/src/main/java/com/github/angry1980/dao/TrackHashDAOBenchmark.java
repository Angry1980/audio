package com.github.angry1980.dao;

import angry1980.audio.model.TrackHash;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Collection;
import java.util.Map;
import java.util.SortedSet;

@State(Scope.Benchmark)
public class TrackHashDAOBenchmark {

    @Param({"1", "2", "3", "4"})
    public int track;

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(".*" + TrackHashDAOBenchmark.class.getSimpleName() + ".*")
                .warmupIterations(5)
                .measurementIterations(5)
                .forks(1)
                .build();
        new Runner(opt).run();
    }

    @Benchmark
    public Collection<TrackHash> testFindByHash(TrackHashDAOState state){
        return state.dao.findByHash(state.hashes[track][10]);
    }

    @Benchmark
    public Map<Long, SortedSet<TrackHash>> testFindByHashes(TrackHashDAOState state){
        return state.dao.findByHashesAndSortByTrack(state.hashes[track]);
    }

}
