package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Piece;

import java.util.HashSet;
import java.util.Set;

public class MoveUtil {
    private static final Move.Visitor<Integer> destinationChecker = new Move.Visitor<>() {
        @Override
        public Integer visit(Move.SingleMove move) {
            return move.destination;
        }

        @Override
        public Integer visit(Move.DoubleMove move) {
            return move.destination2;
        }
    };

    private MoveUtil() {

    }

    public static int moveDestination(Move move) {
        return move.accept(destinationChecker);
    }

    /**
     * Uses visitor to check if a move is a double or not
     * @param move move to be checked
     * @return true if doubleMove, false if singleMove
     */
    public static boolean checkDoubleMove(Move move) {
        Move.Visitor<Boolean> doubleMoveChecker = new Move.Visitor<>() {
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

    private static Set<Integer> getDetectiveLocations(Board board) {
        Set<Integer> detectiveLocations = new HashSet<>();
        for (Piece piece : board.getPlayers()) {
            if (piece.isDetective()) {
                detectiveLocations.add(board.getDetectiveLocation((Piece.Detective) piece).orElseThrow());
            }
        }
        return detectiveLocations;
    }
}
