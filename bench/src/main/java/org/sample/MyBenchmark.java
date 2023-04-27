package org.sample;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import uk.ac.bris.cs.scotlandyard.ui.ai.minimax.GenericMiniMax;
import uk.ac.bris.cs.scotlandyard.ui.ai.minimax.MinimaxFactory;

import java.util.concurrent.TimeUnit;

@Fork(value = 1, warmups = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class MyBenchmark {


    @Benchmark
    public void benchPrunedMinimaxWithoutCaching(Blackhole blackhole, BenchConfiguration state) {
        GenericMiniMax miniMax = new MinimaxFactory().createWithPruning(false);
        blackhole.consume(miniMax.minimaxRoot(true, state.model, state.depth, state.mrX.location(), state.allowDoubleMoves));
    }

    @Benchmark
    public void benchPrunedMinimaxWithCaching(Blackhole blackhole, BenchConfiguration state) {
        GenericMiniMax miniMax = new MinimaxFactory().createWithPruning(true);
        blackhole.consume(miniMax.minimaxRoot(true, state.model, state.depth, state.mrX.location(), state.allowDoubleMoves));
    }

    @Benchmark
    public void benchMinimaxWithCaching(Blackhole blackhole, BenchConfiguration state) {
        GenericMiniMax miniMax = new MinimaxFactory().create(true);
        blackhole.consume(miniMax.minimaxRoot(true, state.model, state.depth, state.mrX.location(), state.allowDoubleMoves));
    }

    @Benchmark
    public void benchMinimaxWithoutCaching(Blackhole blackhole, BenchConfiguration state) {
        GenericMiniMax miniMax = new MinimaxFactory().create(false);
        blackhole.consume(miniMax.minimaxRoot(true, state.model, state.depth, state.mrX.location(), state.allowDoubleMoves));
    }


}
