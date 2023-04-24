package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.Move;

public class MoveUtil {
    public static Integer moveDestination(Move move) {

        return move.accept(destinationChecker);
    }

    public static  Move.Visitor<Integer> destinationChecker = new Move.Visitor<>() {
        @Override
        public Integer visit(Move.SingleMove move) {
            return move.destination;
        }

        @Override
        public Integer visit(Move.DoubleMove move) {
            return move.destination2;
        }
    };
}
