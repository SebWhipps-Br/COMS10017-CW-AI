package org.sample;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@Fork(value = 1, warmups = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class MyBenchmark {


    @Benchmark
    public void benchPrunedMinimaxWithoutCaching(Blackhole blackhole, BenchConfiguration state) {
        blackhole.consume(state.pruningWithoutCaching.minimaxRoot(true, state.model, state.depth, state.mrX.location(), state.allowDoubleMoves));
    }

    @Benchmark
    public void benchPrunedMinimaxWithCaching(Blackhole blackhole, BenchConfiguration state) {
        blackhole.consume(state.pruningWithCaching.minimaxRoot(true, state.model, state.depth, state.mrX.location(), state.allowDoubleMoves));
    }

    @Benchmark
    public void benchMinimaxWithCaching(Blackhole blackhole, BenchConfiguration state) {
        blackhole.consume(state.withCaching.minimaxRoot(true, state.model, state.depth, state.mrX.location(), state.allowDoubleMoves));
    }

    @Benchmark
    public void benchMinimaxWithoutCaching(Blackhole blackhole, BenchConfiguration state) {
        blackhole.consume(state.withoutCaching.minimaxRoot(true, state.model, state.depth, state.mrX.location(), state.allowDoubleMoves));
    }


}
