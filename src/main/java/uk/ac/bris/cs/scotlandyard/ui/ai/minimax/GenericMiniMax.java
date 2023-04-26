package uk.ac.bris.cs.scotlandyard.ui.ai.minimax;

import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;

public interface GenericMiniMax {
    MinimaxResult minimaxRoot(boolean isMrX, Board.GameState root, int depth, int mrXLocation, boolean allowDoubleMoves);

    /**
     * The result of a minimax, which stores a move and a score.
     * This is slightly more efficient than using a Pair type as it avoids having to box the double
     */
    record MinimaxResult(Move move, double score) implements Comparable<MinimaxResult> {
        @Override
        public int compareTo(MinimaxResult o) {
            return Double.compare(this.score, o.score);
        }
    }
}
