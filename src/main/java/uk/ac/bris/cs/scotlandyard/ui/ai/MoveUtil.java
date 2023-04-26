package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.Move;

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

    /**
     * Get Mr X's location after a move
     *
     * @param currentMrXLocation Mr X's current location
     * @param move               The move
     * @return Mr X's (maybe) new location
     */
    public static int getMrXLocationAfter(int currentMrXLocation, Move move) {
        if (move.commencedBy().isMrX()) {
            return MoveUtil.moveDestination(move);
        }
        return currentMrXLocation;
    }


    public static int moveDestination(Move move) {
        return move.accept(destinationChecker);
    }

    /**
     * Uses visitor to check if a move is a double or not
     *
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

}
