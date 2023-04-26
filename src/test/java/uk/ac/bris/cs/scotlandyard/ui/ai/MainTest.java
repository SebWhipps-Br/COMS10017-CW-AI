package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.graph.ImmutableValueGraph;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import org.junit.Test;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.ui.ai.minimax.GenericMiniMax;
import uk.ac.bris.cs.scotlandyard.ui.ai.minimax.MinimaxFactory;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.*;
import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;

public class MainTest {

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

        GenericMiniMax miniMax = new MinimaxFactory().createStandard(true);

        Board.GameState currentBoard = (Board.GameState) model.getCurrentBoard();
        var result = miniMax.minimaxRoot(true, currentBoard, depth, mrX.location(), false);
        assertTrue("Initial move must be done by Mr X", result.move().commencedBy().isMrX());

        for (int i = 0; i < 4; i++) {
            currentBoard = currentBoard.advance(result.move());
            result = miniMax.minimaxRoot(false, currentBoard, depth, mrX.location(), false);
            assertTrue("Move %d must be done by Detective".formatted(i + 1), result.move().commencedBy().isDetective());

        }

        currentBoard = currentBoard.advance(result.move());
        result = miniMax.minimaxRoot(true, currentBoard, depth, mrX.location(), false);
        assertTrue("Sixth move must be done by Mr X", result.move().commencedBy().isMrX());
    }


    private void assertEqualOrBetterScore(boolean biggerIsBetter,
                                          GenericMiniMax.MinimaxResult move1,
                                          GenericMiniMax.MinimaxResult move2) {
        if (biggerIsBetter && move2.score() < move1.score() || !biggerIsBetter && move2.score() > move1.score()) {
            throw new AssertionError("Move2 score was %s than move1 score".formatted(biggerIsBetter ? "lower" : "higher"));
        }
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


        GenericMiniMax cachingMinimax = new MinimaxFactory().createStandard(true);
        GenericMiniMax nonCachingMinimax = new MinimaxFactory().createStandard(false);

        Board.GameState currentBoard = (Board.GameState) model.getCurrentBoard();
        var cached = cachingMinimax.minimaxRoot(true, currentBoard, depth, mrX.location(), false);
        var nonCached = nonCachingMinimax.minimaxRoot(true, currentBoard, depth, mrX.location(), false);
        assertEqualOrBetterScore(true, cached, nonCached);

        for (int i = 0; i < 4; i++) {
            currentBoard = currentBoard.advance(nonCached.move());
            cached = cachingMinimax.minimaxRoot(false, currentBoard, depth, mrX.location(), false);
            nonCached = nonCachingMinimax.minimaxRoot(false, currentBoard, depth, mrX.location(), false);
            assertEqualOrBetterScore(false, cached, nonCached);
        }


        cached = cachingMinimax.minimaxRoot(true, currentBoard, depth, mrX.location(), false);
        nonCached = nonCachingMinimax.minimaxRoot(true, currentBoard, depth, mrX.location(), false);
        assertEqualOrBetterScore(true, cached, nonCached);
    }

    @Test
    public void testDijkstra() {
        MutableValueGraph<Integer, Integer> graph = ValueGraphBuilder.undirected().build();

        graph.addNode(1);
        graph.addNode(2);
        graph.addNode(3);

        graph.putEdgeValue(1, 2, 1);
        graph.putEdgeValue(2, 3, 1);
        graph.putEdgeValue(1, 3, 5);


        var shortestPath = Dijkstra.pureDijkstra(ImmutableValueGraph.copyOf(graph), i -> i, 1);

        assertEquals(shortestPath, Map.of(1, 0, 2, 1, 3, 2));
    }
}
