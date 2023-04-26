package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.graph.ImmutableValueGraph;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class DijkstraTest {
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
