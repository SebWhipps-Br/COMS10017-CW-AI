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

    // filter the graph, removing impossible moves
    private static ValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> filterImpossibleMoves(Board board, ValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> graph) {
        Set<EndpointPair<Integer>> possibleMoves = new HashSet<>();

        // create a set of all the possible moves
        for (Move availableMove : board.getAvailableMoves()) {
            if (availableMove instanceof Move.SingleMove singleMove) {
                possibleMoves.add(EndpointPair.unordered(singleMove.source(), singleMove.destination));
            }
            if (availableMove instanceof Move.DoubleMove doubleMove) {
                possibleMoves.add(EndpointPair.unordered(doubleMove.source(), doubleMove.destination1));
                possibleMoves.add(EndpointPair.unordered(doubleMove.destination1, doubleMove.destination2));
            }
        }

        // go over the graph and remove any *im*possible moves
        var possibleMovesGraph = Graphs.copyOf(graph);
        for (EndpointPair<Integer> edge : graph.edges()) {
            if (!possibleMoves.contains(edge)) {
                possibleMovesGraph.removeEdge(edge);
            }
        }
        return possibleMovesGraph;

    }

    /*
    Calculates a map of the shortest path to all other nodes on the board from a given source
     */
    public static Map<Integer, Integer> dijkstra(Board board, int source) {


        // Stores the node value and its distance, but only compares the distance. Used for the priority queue
        record NodeInfo(int v, int dist) implements Comparable<NodeInfo> {
            @Override
            public int compareTo(NodeInfo o) {
                return Integer.compare(this.dist, o.dist);
            }
        }

        Map<Integer, Integer> dist = new HashMap<>();
        Map<Integer, Integer> prev = new HashMap<>();
        PriorityQueue<NodeInfo> priorityQueue = new PriorityQueue<>();

        var graph = filterImpossibleMoves(board, board.getSetup().graph);
        if (graph.edges().isEmpty()) {
            throw new IllegalArgumentException("Graph contains no possible moves");
        }

        dist.put(source, 0);

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

    public static Double dijkstraScore(List<Integer> distances) {
        Double mean = distances.stream().mapToDouble(x -> x).average().orElseThrow();
        Double shortest = Double.valueOf(Collections.min(distances));
        return shortest + (mean / 10);
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

}
