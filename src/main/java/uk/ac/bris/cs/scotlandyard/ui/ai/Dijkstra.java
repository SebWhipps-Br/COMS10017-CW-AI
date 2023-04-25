package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import javafx.util.Pair;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

import java.util.*;
import java.util.function.ToIntFunction;

public class Dijkstra {

    //TODO perform dijkstra on current node to decide if to use a double move
    // if score is < 2 use double move

    // filter the graph, removing impossible moves
    private static ValueGraph<Integer, ImmutableSet<ScotlandYard.Ticket>> filterImpossibleMoves(Board board, ValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> graph) {
        Set<Pair<EndpointPair<Integer>, Iterable<ScotlandYard.Ticket>>> possibleMoves = new HashSet<>();

        // create a set of all the possible moves
        for (Move availableMove : board.getAvailableMoves()) {
            if (availableMove instanceof Move.SingleMove singleMove) {
                possibleMoves.add(
                        new Pair<>(EndpointPair.unordered(singleMove.source(), singleMove.destination), availableMove.tickets()));
            }
            if (availableMove instanceof Move.DoubleMove doubleMove) {
                possibleMoves.add(
                        new Pair<>(EndpointPair.unordered(doubleMove.source(), doubleMove.destination1), Set.of(doubleMove.ticket1)));
                possibleMoves.add(new Pair<>(EndpointPair.unordered(doubleMove.destination1, doubleMove.destination2), Set.of(doubleMove.ticket2)));
            }
        }

        // build graph of possible moves
        MutableValueGraph<Integer, ImmutableSet<ScotlandYard.Ticket>> possibleMovesGraph = ValueGraphBuilder.undirected().build();
        for (Pair<EndpointPair<Integer>, Iterable<ScotlandYard.Ticket>> possibleMove : possibleMoves) {
            possibleMovesGraph.putEdgeValue(possibleMove.getKey(), ImmutableSet.copyOf(possibleMove.getValue()));
        }
        for (Integer node : graph.nodes()) {
            possibleMovesGraph.addNode(node);
        }

        return possibleMovesGraph;
    }

    /*
    Calculates a map of the shortest path to all other nodes on the board from a given source

    TODO: use a fibonacci heap for the queue for extra performance
     */
    public static Map<Integer, Integer> dijkstra(boolean considerTickets, Board board, int source) {
        return pureDijkstra(considerTickets ? filterImpossibleMoves(board, board.getSetup().graph) : board.getSetup().graph, AbstractCollection::size, source);
    }

    public static <T> Map<Integer, Integer> pureDijkstra(ValueGraph<Integer, ? extends T> graph, ToIntFunction<T> scoring, int source) {
        // Stores the node value and its distance, but only compares the distance. Used for the priority queue
        record NodeInfo(int nodeValue, int dist) implements Comparable<NodeInfo> {
            @Override
            public int compareTo(NodeInfo o) {
                return Integer.compare(this.dist, o.dist);
            }

        }

        if (graph.edges().isEmpty()) {
            throw new IllegalArgumentException("Graph contains no possible moves");
        }

        Map<Integer, Integer> dist = new HashMap<>();
        Map<Integer, Integer> prev = new HashMap<>();
        dist.put(source, 0);

        PriorityQueue<NodeInfo> priorityQueue = new PriorityQueue<>();

        for (Integer node : graph.nodes()) {
            if (node != source) {
                dist.put(node, Integer.MAX_VALUE);
                prev.put(node, null);
            }
            priorityQueue.add(new NodeInfo(node, dist.get(node)));
        }

        while (!priorityQueue.isEmpty()) {
            NodeInfo u = priorityQueue.remove(); // extract the minimum / best vertex
            if (!graph.nodes().contains(u.nodeValue)) {
                throw new IllegalArgumentException("Not in graph");
            }
            for (Integer neighbour : graph.adjacentNodes(u.nodeValue)) {
                int uDist = dist.get(u.nodeValue);
                int alt = uDist == Integer.MAX_VALUE ? Integer.MAX_VALUE : uDist + graph.edgeValue(u.nodeValue, neighbour)
                        .map(scoring::applyAsInt)
                        .orElse(0);

                if (alt < dist.get(neighbour)) {
                    dist.put(neighbour, alt);
                    prev.put(neighbour, u.nodeValue);

                    priorityQueue.add(new NodeInfo(neighbour, alt));
                }
            }
        }

        dist.entrySet().removeIf(e -> e.getValue() == Integer.MAX_VALUE); // remove paths that aren't accessible at all
        return dist;
    }

    /**
     * evaluates a list of distances and gives it a score
     *
     * @param distances a list of distances
     * @return a score
     */
    public static Double dijkstraScore(List<Integer> distances) {
        double mean = distances.stream().mapToDouble(x -> x).average().orElseThrow();
        double shortest = Collections.min(distances);
        return shortest + mean / 10;
    }

}
