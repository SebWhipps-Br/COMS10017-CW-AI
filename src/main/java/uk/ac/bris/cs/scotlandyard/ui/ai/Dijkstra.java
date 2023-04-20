package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.*;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class Dijkstra {

    /*
    Calculates a map of the shortest path to all other nodes on the board from a given source
     */
    public static PathGraph dijkstra(Board board, int source) {
        Map<Integer, Integer> dist = new HashMap<>();
        Map<Integer, Integer> prev = new HashMap<>();
        record NodeInfo(int v, int dist) implements Comparable<NodeInfo> {
            @Override
            public int compareTo(NodeInfo o) {
                return Integer.compare(this.dist, o.dist);
            }
        }

        PriorityQueue<NodeInfo> priorityQueue = new PriorityQueue<>();

        var graph = board.getSetup().graph;
        for (Integer node : graph.nodes()) {
            if (node != source) {
                dist.put(node, Integer.MAX_VALUE);
                prev.put(node, null);
            }
            priorityQueue.add(new NodeInfo(node, dist.get(node)));
        }

        while (!priorityQueue.isEmpty()) {
            NodeInfo u = priorityQueue.poll(); // extract the minimum / best vertex
            for (Integer v : graph.adjacentNodes(u.v)) {
                int alt = dist.get(u.v) + graph.edgeValue(u.v, v).orElse(ImmutableSet.of()).size();
                if (alt < dist.get(u.v)) {
                    dist.put(v, alt);
                    prev.put(v, u.v);
                    priorityQueue.remove(u);
                    u = new NodeInfo(u.v, alt);
                    priorityQueue.add(u);
                }
            }
        }


        MutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> newGraph = ValueGraphBuilder.undirected().build();
        for (Map.Entry<Integer, Integer> entry : prev.entrySet()) {
            var edge = EndpointPair.unordered(entry.getKey(), entry.getValue());
            newGraph.putEdgeValue(edge, graph.edgeValueOrDefault(edge, ImmutableSet.of()));
        }
        return new PathGraph(source, ImmutableValueGraph.copyOf(newGraph));
    }

    /**
     * Stores the shortest path to each other node from a given source
     *
     * @param moves
     */
    record PathGraph(int source, ValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> moves) {
    }
}
