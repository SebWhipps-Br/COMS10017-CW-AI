package uk.ac.bris.cs.scotlandyard.ui.ai;

import org.junit.Test;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.ui.ai.minimax.GenericMiniMax;
import uk.ac.bris.cs.scotlandyard.ui.ai.minimax.MinimaxFactory;

import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.*;
import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;

public class MinimaxTest {

    @Test
    public void testMinimax() throws IOException {
        MyModelFactory modelFactory = new MyModelFactory();

        var mrX = new Player(MRX, defaultMrXTickets(), 106);
        var red = new Player(RED, defaultDetectiveTickets(), 91);
        var green = new Player(GREEN, defaultDetectiveTickets(), 29);
        var blue = new Player(BLUE, defaultDetectiveTickets(), 94);
        var white = new Player(WHITE, defaultDetectiveTickets(), 50);

        int depth = 8;
        final Model model = modelFactory.build(new GameSetup(ScotlandYard.standardGraph(), STANDARD24MOVES), mrX, red, green, blue, white);

        GenericMiniMax miniMax = new MinimaxFactory().createWithPruning(true);

        Board.GameState currentBoard = (Board.GameState) model.getCurrentBoard();
        var result = miniMax.minimaxRoot(true, currentBoard, depth, mrX.location(), false);
        validateResult(0, 4, result);


        for (int i = 0; i < 4; i++) {
            currentBoard = currentBoard.advance(result.move());
            result = miniMax.minimaxRoot(false, currentBoard, depth, mrX.location(), false);
            validateResult(i + 1, 4, result);
        }

        currentBoard = currentBoard.advance(result.move());
        result = miniMax.minimaxRoot(true, currentBoard, depth, mrX.location(), false);
        validateResult(6, 4, result);
    }

    private void validateResult(int i, int detectiveCount, GenericMiniMax.MinimaxResult result) {
        if (i == 0) {
            assertTrue("Initial move must be done by Mr X", result.move().commencedBy().isMrX());
            return;
        }
        if (i < detectiveCount) {
            assertTrue("Move %d must be done by Detective".formatted(i), result.move().commencedBy().isDetective());
            return;
        }

        if (i % detectiveCount == 1) {
            assertTrue("Move %d must be done by Mr X".formatted(i), result.move().commencedBy().isMrX());
            return;
        }
        assertTrue("Move %d must be done by Detective".formatted(i), result.move().commencedBy().isDetective());
    }


    private void assertEqualOrBetterScore(boolean biggerIsBetter,
                                          GenericMiniMax.MinimaxResult baseline,
                                          GenericMiniMax.MinimaxResult other) {
        if (biggerIsBetter && other.score() < baseline.score() || !biggerIsBetter && other.score() > baseline.score()) {
            throw new AssertionError("Move2 score was %s than move1 score".formatted(biggerIsBetter ? "lower" : "higher"));
        }
    }

    @Test
    public void testAlphaBetaCachingDoesntMakeResultsWorse() throws IOException {
        MyModelFactory modelFactory = new MyModelFactory();

        var mrX = new Player(MRX, defaultMrXTickets(), 106);
        var red = new Player(RED, defaultDetectiveTickets(), 91);
        var green = new Player(GREEN, defaultDetectiveTickets(), 29);
        var blue = new Player(BLUE, defaultDetectiveTickets(), 94);
        var white = new Player(WHITE, defaultDetectiveTickets(), 50);

        int depth = 5;
        final Model model = modelFactory.build(new GameSetup(ScotlandYard.standardGraph(), STANDARD24MOVES), mrX, red, green, blue, white);


        GenericMiniMax cachingMinimax = new MinimaxFactory().createWithPruning(true);
        GenericMiniMax nonCachingMinimax = new MinimaxFactory().createWithPruning(false);

        Board.GameState currentBoard = (Board.GameState) model.getCurrentBoard();

        var cached = cachingMinimax.minimaxRoot(true, currentBoard, depth, mrX.location(), false);
        var nonCached = nonCachingMinimax.minimaxRoot(true, currentBoard, depth, mrX.location(), false);
        validateResult(0, 4, cached);
        validateResult(0, 4, nonCached);
        assertEqualOrBetterScore(true, nonCached, cached);

        for (int i = 0; i < 4; i++) {
            currentBoard = currentBoard.advance(nonCached.move());
            cached = cachingMinimax.minimaxRoot(false, currentBoard, depth, mrX.location(), false);
            nonCached = nonCachingMinimax.minimaxRoot(false, currentBoard, depth, mrX.location(), false);
            validateResult(i + 1, 4, cached);
            validateResult(i + 1, 4, nonCached);
            assertEqualOrBetterScore(false, nonCached, cached);
        }


        cached = cachingMinimax.minimaxRoot(true, currentBoard, depth, mrX.location(), false);
        nonCached = nonCachingMinimax.minimaxRoot(true, currentBoard, depth, mrX.location(), false);
        assertEqualOrBetterScore(true, cached, nonCached);
        validateResult(6, 4, cached);
        validateResult(6, 4, nonCached);
    }

    @Test
    public void testMinimaxPruningDoesntChangeResults() throws IOException {
        MyModelFactory modelFactory = new MyModelFactory();

        var mrX = new Player(MRX, defaultMrXTickets(), 106);
        var red = new Player(RED, defaultDetectiveTickets(), 91);
        var green = new Player(GREEN, defaultDetectiveTickets(), 29);
        var blue = new Player(BLUE, defaultDetectiveTickets(), 94);
        var white = new Player(WHITE, defaultDetectiveTickets(), 50);

        int depth = 5;
        final Model model = modelFactory.build(new GameSetup(ScotlandYard.standardGraph(), STANDARD24MOVES), mrX, red, green, blue, white);


        GenericMiniMax pruningMinimax = new MinimaxFactory().createWithPruning(false);
        GenericMiniMax nonPruningMinimax = new MinimaxFactory().create(false);

        Board.GameState currentBoard = (Board.GameState) model.getCurrentBoard();
        var cached = pruningMinimax.minimaxRoot(true, currentBoard, depth, mrX.location(), false);
        var nonCached = nonPruningMinimax.minimaxRoot(true, currentBoard, depth, mrX.location(), false);
        assertEqualOrBetterScore(true, nonCached, cached);

        for (int i = 0; i < 4; i++) {
            currentBoard = currentBoard.advance(nonCached.move());
            cached = pruningMinimax.minimaxRoot(false, currentBoard, depth, mrX.location(), false);
            nonCached = nonPruningMinimax.minimaxRoot(false, currentBoard, depth, mrX.location(), false);
            assertEqualOrBetterScore(false, nonCached, cached);
        }


        cached = pruningMinimax.minimaxRoot(true, currentBoard, depth, mrX.location(), false);
        nonCached = nonPruningMinimax.minimaxRoot(true, currentBoard, depth, mrX.location(), false);
        assertEqualOrBetterScore(true, nonCached, cached);
    }

    @Test
    public void testMinimaxCachingDoesntMakeResultsWorse() throws IOException {
        MyModelFactory modelFactory = new MyModelFactory();

        var mrX = new Player(MRX, defaultMrXTickets(), 106);
        var red = new Player(RED, defaultDetectiveTickets(), 91);
        var green = new Player(GREEN, defaultDetectiveTickets(), 29);
        var blue = new Player(BLUE, defaultDetectiveTickets(), 94);
        var white = new Player(WHITE, defaultDetectiveTickets(), 50);

        int depth = 4;
        final Model model = modelFactory.build(new GameSetup(ScotlandYard.standardGraph(), STANDARD24MOVES), mrX, red, green, blue, white);


        GenericMiniMax pruningMinimax = new MinimaxFactory().create(false);
        GenericMiniMax nonPruningMinimax = new MinimaxFactory().create(true);

        Board.GameState currentBoard = (Board.GameState) model.getCurrentBoard();
        var cached = pruningMinimax.minimaxRoot(true, currentBoard, depth, mrX.location(), false);
        var nonCached = nonPruningMinimax.minimaxRoot(true, currentBoard, depth, mrX.location(), false);
        assertEqualOrBetterScore(true, nonCached, cached);

        for (int i = 0; i < 4; i++) {
            currentBoard = currentBoard.advance(nonCached.move());
            cached = pruningMinimax.minimaxRoot(false, currentBoard, depth, mrX.location(), false);
            nonCached = nonPruningMinimax.minimaxRoot(false, currentBoard, depth, mrX.location(), false);
            assertEqualOrBetterScore(false, nonCached, cached);
        }


        cached = pruningMinimax.minimaxRoot(true, currentBoard, depth, mrX.location(), false);
        nonCached = nonPruningMinimax.minimaxRoot(true, currentBoard, depth, mrX.location(), false);
        assertEqualOrBetterScore(true, nonCached, cached);
    }


}
