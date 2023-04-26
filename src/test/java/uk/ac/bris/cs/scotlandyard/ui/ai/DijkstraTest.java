package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.graph.ImmutableValueGraph;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import org.junit.Test;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.ui.ai.minimax.GenericMiniMax;
import uk.ac.bris.cs.scotlandyard.ui.ai.minimax.AlphaBetaMinimax;
import uk.ac.bris.cs.scotlandyard.ui.ai.minimax.MinimaxFactory;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.*;
import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;

public class DijkstraTest {

    @Test
    public void testPerformance() throws IOException {
        MyModelFactory modelFactory = new MyModelFactory();

        var mrX = new Player(MRX, defaultMrXTickets(), 106);
        var red = new Player(RED, defaultDetectiveTickets(), 91);
        var green = new Player(GREEN, defaultDetectiveTickets(), 29);
        var blue = new Player(BLUE, defaultDetectiveTickets(), 94);
        var white = new Player(WHITE, defaultDetectiveTickets(), 50);

        int depth = 6;
        final Model model = modelFactory.build(new GameSetup(ScotlandYard.standardGraph(), STANDARD24MOVES), mrX, red, green, blue, white);

        GenericMiniMax miniMax = new MinimaxFactory().createStandard(true);

        Board.GameState currentBoard = (Board.GameState) model.getCurrentBoard();
        AlphaBetaMinimax.MinimaxResult minimax = miniMax.minimaxRoot(true, currentBoard, depth, mrX.location(), false);
        assertTrue("Initial move must be done by Mr X", minimax.move().commencedBy().isMrX());
        System.out.println(minimax.move());


        Board.GameState advance = currentBoard.advance(minimax.move());
        AlphaBetaMinimax.MinimaxResult minimax2 = miniMax.minimaxRoot(false, advance, depth, mrX.location(), false);
        assertTrue("Second move must be done by Detective", minimax2.move().commencedBy().isDetective());
        System.out.println(minimax2.move());

        Board.GameState advance2 = advance.advance(minimax2.move());
        AlphaBetaMinimax.MinimaxResult minimax3 = miniMax.minimaxRoot(false, advance2, depth, mrX.location(), false);
        assertTrue("Third move must be done by Detective", minimax3.move().commencedBy().isDetective());
        System.out.println(minimax3.move());

        Board.GameState advance3 = advance2.advance(minimax3.move());
        AlphaBetaMinimax.MinimaxResult minimax4 = miniMax.minimaxRoot(false, advance3, depth, mrX.location(), false);
        assertTrue("Fourth move must be done by Detective", minimax4.move().commencedBy().isDetective());
        System.out.println(minimax4.move());

        Board.GameState advance4 = advance3.advance(minimax4.move());
        AlphaBetaMinimax.MinimaxResult minimax5 = miniMax.minimaxRoot(false, advance4, depth, mrX.location(), false);
        assertTrue("Fifth move must be done by Detective", minimax5.move().commencedBy().isDetective());
        System.out.println(minimax5.move());

        Board.GameState advance5 = advance4.advance(minimax5.move());
        AlphaBetaMinimax.MinimaxResult minimax6 = miniMax.minimaxRoot(true, advance5, depth, mrX.location(), false);
        assertTrue("Sixth move must be done by Mr X", minimax6.move().commencedBy().isMrX());
        System.out.println(minimax6.move());
        /*
SECRET(MRX@106, 105)
BUS(BLUE@94, 77)
TAXI(GREEN@29, 42)
TAXI(RED@91, 72)
TAXI(WHITE@50, 49)
SECRET(MRX@105, 108)
         */
    }

    @Test
    public void testDijkstraPerformance() {
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
