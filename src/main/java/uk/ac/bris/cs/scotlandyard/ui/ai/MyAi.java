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

    public static Double dijkstra(Board board, Move move) {
        Set<Integer> detectiveLocations = getDetectiveLocations(board);

        List<Integer> unvisited = new ArrayList<>(board.getSetup().graph.nodes());
        System.out.println(unvisited.toString());

        return 1.0;
    }




    private static Double dijkstraScore(Set<Double> distances) {
        //Double sum = distances.stream().collect(summingDouble(x -> x));
        Double shortest = Collections.min(distances);
        return shortest;
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
//		System.out.println(moves);
//		System.out.println(moves.get(0));
//		System.out.println(moves.get(0).hashCode());
//		System.out.println(moves.get(0).source());
//		System.out.println(moves.get(0).toString());
//		System.out.println(checkDoubleMove(moves.get(0)));
//
//		System.out.println(getDetectiveLocations(board));
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


}
