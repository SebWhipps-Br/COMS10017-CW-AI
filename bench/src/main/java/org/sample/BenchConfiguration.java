package org.sample;

import org.openjdk.jmh.annotations.*;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.ui.ai.minimax.GenericMiniMax;
import uk.ac.bris.cs.scotlandyard.ui.ai.minimax.MinimaxFactory;

import java.io.IOException;

import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.*;
import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;

@State(Scope.Benchmark)
public class BenchConfiguration {
    public final GenericMiniMax pruningWithCaching = new MinimaxFactory().createWithPruning(true);
    public final GenericMiniMax pruningWithoutCaching = new MinimaxFactory().createWithPruning(false);
    public final GenericMiniMax withoutCaching = new MinimaxFactory().create(false);
    public final GenericMiniMax withCaching = new MinimaxFactory().create(true);
    @Param({"2", "3", "4", "5", "6"})
    public int depth;
    public boolean allowDoubleMoves;
    public Board.GameState model;

    public Player mrX;

    @Setup(Level.Invocation)
    public void init() throws IOException {
        MyModelFactory modelFactory = new MyModelFactory();
        mrX = new Player(MRX, defaultMrXTickets(), 106);
        var red = new Player(RED, defaultDetectiveTickets(), 91);
        var green = new Player(GREEN, defaultDetectiveTickets(), 29);
        var blue = new Player(BLUE, defaultDetectiveTickets(), 94);
        var white = new Player(WHITE, defaultDetectiveTickets(), 50);

        final Model model = modelFactory.build(new GameSetup(ScotlandYard.standardGraph(), STANDARD24MOVES), mrX, red, green, blue, white);
        this.model = (Board.GameState) model.getCurrentBoard();
    }
}