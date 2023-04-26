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

        int depth = 3;
        final Model model = modelFactory.build(new GameSetup(ScotlandYard.standardGraph(), STANDARD24MOVES), mrX, red, green, blue, white);

        GenericMiniMax miniMax = new MinimaxFactory().createWithPruning(true);

        Board.GameState currentBoard = (Board.GameState) model.getCurrentBoard();
        var result = miniMax.minimaxRoot(true, currentBoard, depth, mrX.location(), false);
        validateResult(true, 0, result);


        for (int i = 0; i < 4; i++) {
            currentBoard = currentBoard.advance(result.move());
            result = miniMax.minimaxRoot(false, currentBoard, depth, mrX.location(), false);
            validateResult(false, i + 1, result);
        }

        currentBoard = currentBoard.advance(result.move());
        result = miniMax.minimaxRoot(true, currentBoard, depth, mrX.location(), false);
        validateResult(true, 6, result);
    }

    private void validateResult(boolean shouldBeMrX, int i, GenericMiniMax.MinimaxResult result) {
        if (shouldBeMrX) {
            assertTrue("Move %d must be done by Mr X".formatted(i), result.move().commencedBy().isMrX());
        } else {
            assertTrue("Move %s %d must be done by Detective".formatted(result.move().toString(), i), result.move().commencedBy().isDetective());
        }


    }


    private void assertEqualOrBetterScore(boolean biggerIsBetter,
                                          GenericMiniMax.MinimaxResult baseline,
                                          GenericMiniMax.MinimaxResult other) {
        if (biggerIsBetter && other.score() < baseline.score() || !biggerIsBetter && other.score() > baseline.score()) {
            throw new AssertionError("Move2 score was %s than move1 score when it should've been %s"
                    .formatted(biggerIsBetter ? "lower" : "higher"
                            , biggerIsBetter ? "higher" : "lower"));
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
        validateResult(true, 0, cached);
        validateResult(true, 0, nonCached);
        assertEqualOrBetterScore(true, nonCached, cached);

        for (int i = 0; i < 4; i++) {
            currentBoard = currentBoard.advance(nonCached.move());
            cached = cachingMinimax.minimaxRoot(false, currentBoard, depth, mrX.location(), false);
            nonCached = nonCachingMinimax.minimaxRoot(false, currentBoard, depth, mrX.location(), false);
            validateResult(false, i + 1, cached);
            validateResult(false, i + 1, nonCached);
            assertEqualOrBetterScore(false, nonCached, cached);
        }


        cached = cachingMinimax.minimaxRoot(true, currentBoard, depth, mrX.location(), false);
        nonCached = nonCachingMinimax.minimaxRoot(true, currentBoard, depth, mrX.location(), false);
        assertEqualOrBetterScore(true, cached, nonCached);
        validateResult(false, 6, cached);
        validateResult(false, 6, nonCached);
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

        int depth = 5;
        final Model model = modelFactory.build(new GameSetup(ScotlandYard.standardGraph(), STANDARD24MOVES), mrX, red, green, blue, white);


        GenericMiniMax cachingMinimax = new MinimaxFactory().create(false);
        GenericMiniMax nonCachingMinimax = new MinimaxFactory().create(true);

        Board.GameState currentBoard = (Board.GameState) model.getCurrentBoard();
        var cached = cachingMinimax.minimaxRoot(true, currentBoard, depth, mrX.location(), false);
        var nonCached = nonCachingMinimax.minimaxRoot(true, currentBoard, depth, mrX.location(), false);
        assertEqualOrBetterScore(true, nonCached, cached);

        for (int i = 0; i < 4; i++) {
            currentBoard = currentBoard.advance(nonCached.move());
            cached = cachingMinimax.minimaxRoot(false, currentBoard, depth, mrX.location(), false);
            nonCached = nonCachingMinimax.minimaxRoot(false, currentBoard, depth, mrX.location(), false);
            assertEqualOrBetterScore(false, nonCached, cached);
        }


        cached = cachingMinimax.minimaxRoot(true, currentBoard, depth, mrX.location(), false);
        nonCached = nonCachingMinimax.minimaxRoot(true, currentBoard, depth, mrX.location(), false);
        assertEqualOrBetterScore(true, nonCached, cached);
    }

    @Test
    public void testAllFourGetSameOrBetterResults() throws IOException {
        MyModelFactory modelFactory = new MyModelFactory();

        var mrX = new Player(MRX, defaultMrXTickets(), 106);
        var red = new Player(RED, defaultDetectiveTickets(), 91);
        var green = new Player(GREEN, defaultDetectiveTickets(), 29);
        var blue = new Player(BLUE, defaultDetectiveTickets(), 94);
        var white = new Player(WHITE, defaultDetectiveTickets(), 50);

        int depth = 6;
        final Model model = modelFactory.build(new GameSetup(ScotlandYard.standardGraph(), STANDARD24MOVES), mrX, red, green, blue, white);


        GenericMiniMax none = new MinimaxFactory().create(false);
        GenericMiniMax caching = new MinimaxFactory().create(true);
        GenericMiniMax pruningAndCaching = new MinimaxFactory().createWithPruning(true);
        GenericMiniMax pruning = new MinimaxFactory().createWithPruning(false);

        Board.GameState currentBoard = (Board.GameState) model.getCurrentBoard();
        var noned = none.minimaxRoot(true, currentBoard, depth, mrX.location(), false);
        var cached = caching.minimaxRoot(true, currentBoard, depth, mrX.location(), false);
        var prunedAndCached = pruningAndCaching.minimaxRoot(true, currentBoard, depth, mrX.location(), false);
        var pruned = pruning.minimaxRoot(true, currentBoard, depth, mrX.location(), false);
        assertEqualOrBetterScore(true, noned, cached);
        assertEqualOrBetterScore(true, noned, prunedAndCached);
        assertEqualOrBetterScore(true, noned, pruned);

        assertEqualOrBetterScore(true, cached, pruned);
        assertEqualOrBetterScore(true, cached, prunedAndCached);
        assertEqualOrBetterScore(true, cached, noned);

        assertEqualOrBetterScore(true, pruned, noned);
        assertEqualOrBetterScore(true, pruned, cached);
        assertEqualOrBetterScore(true, pruned, prunedAndCached);

        assertEqualOrBetterScore(true, prunedAndCached, noned);
        assertEqualOrBetterScore(true, prunedAndCached, cached);
        assertEqualOrBetterScore(true, prunedAndCached, pruned);



    }


}
