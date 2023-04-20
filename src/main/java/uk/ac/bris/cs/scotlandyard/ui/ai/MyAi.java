package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableSet;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Ai;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Piece;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class MyAi implements Ai {

    private static Integer moveDestination(Move move) {
        Move.Visitor<Integer> destinationChecker = new Move.Visitor<Integer>() {
            @Override
            public Integer visit(Move.SingleMove move) {
                return move.destination;
            }

            @Override
            public Integer visit(Move.DoubleMove move) {
                return move.destination2;
            }
        };
        return move.accept(destinationChecker);
    }

    private static Set<Integer> getDetectiveLocations(Board board) {
        Set<Integer> detectiveLocations = new HashSet<>();
        for (Piece piece : board.getPlayers()) {
            if (piece.isDetective()) {
                detectiveLocations.add(board.getDetectiveLocation((Piece.Detective) piece).get());
            }
        }
        return detectiveLocations;
    }





    @Nonnull
    @Override
    public String name() {
        return "Dijkstratron";
    }

    @Nonnull
    @Override
    public Move pickMove(
            @Nonnull Board board,
            Pair<Long, TimeUnit> timeoutPair) {

        List<Move> moves = board.getAvailableMoves().asList();
        //dijkstra for each possible move


        MoveTree tree = MoveTree.generate((Board.GameState) board, 3);
//
//        Dictionary<Move, Double> moveScores = new Hashtable<>();
//        Double bestScore = 0.0; //higher is better
//        Move best = moves.get(0);
//        for (Move move : moves) {
//            Double d = dijkstra(board, move);
//            if (d > bestScore) {
//                bestScore = d;
//                best = move;
//            }
//            moveScores.put(move, d);
//
//        }
//
//        return best;
        // returns a random move, replace with your own implementation
        return moves.get(new Random().nextInt(moves.size()));
    }

    /*
    Calculates the shortest path / distance from a Mr X source position to a destination (probably a detective location)
    */
    public static double dijkstra(Board board, int source, int destination) {
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

        return 1.0;

    }


    private boolean checkDoubleMove(Move move) {
        Move.Visitor<Boolean> doubleMoveChecker = new Move.Visitor<Boolean>() {
            @Override
            public Boolean visit(Move.SingleMove move) {
                return false;
            }

            @Override
            public Boolean visit(Move.DoubleMove move) {
                return true;
            }
        };
        return move.accept(doubleMoveChecker);
    }


	private Double dijkstraScore(Set<Double> distances){
		Double mean = distances.stream().mapToDouble(x -> x).average().orElseThrow();
		Double shortest = Collections.min(distances);
		return shortest + (mean / 10);
	}


}
