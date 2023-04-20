package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.*;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

import java.util.*;

public class Dijkstra {
    //TODO perform dijkstra on current node to decide if to use a double move
    // if score is < 2 use double move

    private static MutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> filterImpossibleMoves(Board board, ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> graph) {
        // filter the graph, removing impossible moves
        Set<EndpointPair<Integer>> edges = new HashSet<>(graph.edges());
        for (Move availableMove : board.getAvailableMoves()) {
            if (availableMove instanceof Move.SingleMove singleMove) {
                edges.remove(EndpointPair.unordered(singleMove.source(), singleMove.destination));
            }
            if (availableMove instanceof Move.DoubleMove doubleMove) {
                edges.remove(EndpointPair.unordered(doubleMove.source(), doubleMove.destination1));
                edges.remove(EndpointPair.unordered(doubleMove.destination1, doubleMove.destination2));
            }
        }
        var setMutableValueGraph = ValueGraphBuilder.from(graph).build();
        for (EndpointPair<Integer> edge : edges) {
            setMutableValueGraph.removeEdge(edge);
        }
        return setMutableValueGraph;

    }

    /*
    Calculates a map of the shortest path to all other nodes on the board from a given source
     */
    public static Map<Integer, Integer> dijkstra(Board board, int source) {
        Map<Integer, Integer> dist = new HashMap<>();
        Map<Integer, Integer> prev = new HashMap<>();
        record NodeInfo(int v, int dist) implements Comparable<NodeInfo> {
            @Override
            public int compareTo(NodeInfo o) {
                return Integer.compare(this.dist, o.dist);
            }
        }

        PriorityQueue<NodeInfo> priorityQueue = new PriorityQueue<>();

        var graph = filterImpossibleMoves(board, board.getSetup().graph);

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
        return dist;
//        return new PathGraph(source, ImmutableValueGraph.copyOf(newGraph));
    }

    /**
     * Stores the shortest path to each other node from a given source
     *
     * @param moves
     */
    record PathGraph(int source, ValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> moves) {
        /**
         * Searches from the source to the given from, returning all the transports needed
         */
        ImmutableSet<ScotlandYard.Transport> search(int from) {
            Iterable<Integer> integers = Traverser.forGraph(moves::adjacentNodes)
                    .depthFirstPostOrder(from);

return null; // TODO
        }
    }


    public static Double dijkstraScore(List<Integer> distances){
        Double mean = distances.stream().mapToDouble(x -> x).average().orElseThrow();
        Double shortest = Double.valueOf(Collections.min(distances));
        return shortest + (mean / 10);
    }

}
