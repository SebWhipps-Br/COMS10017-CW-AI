package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.graph.ImmutableValueGraph;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import org.junit.Test;
import uk.ac.bris.cs.scotlandyard.model.*;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;
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
        var yellow = new Player(YELLOW, defaultDetectiveTickets(), 138);

        final Model model = modelFactory.build(new GameSetup(ScotlandYard.standardGraph(), STANDARD24MOVES), mrX, red, green, blue, white, yellow);
        MoveTree generate = MoveTree.generate((Board.GameState) model.getCurrentBoard(), 5, true);
        System.out.println(generate.size());

    }

    @Test
    public void testDijkstraPerformance() throws IOException {
        MutableValueGraph<Integer, Integer> graph = ValueGraphBuilder.undirected().build();

        graph.addNode(1);
        graph.addNode(2);
        graph.addNode(3);

        graph.putEdgeValue(1, 2, 1);
        graph.putEdgeValue(2, 3, 1);
        graph.putEdgeValue(1, 3, 5);


        var shortestPath = Dijkstra.pureDijkstra(ImmutableValueGraph.copyOf(graph), i -> i, 1);

        assertEquals(shortestPath, Map.of(1, 0, 2, 1, 3,2 ));
    }
}
