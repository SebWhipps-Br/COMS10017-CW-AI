package uk.ac.bris.cs.scotlandyard.ui.ai.minimax.scoring;

import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.Move;

import java.util.Collection;

public interface MoveScorer {

    /**
     * Calculate a score for a move. The algorithm for how to do this is left up to the implementation, but it must
     * respect the following laws:
     * <ul>
     *     <li>If the move results in the player (according to {@code isMrX}) winning, it must return {@link Double#POSITIVE_INFINITY}</li>
     *     <li>If the move results in the player losing, it must return {@link Double#NEGATIVE_INFINITY}</li>
     * </ul>
     *
     * @param isMrX            If we are scoring for Mr X or not. This typically "inverts" the score, i.e. a good score for Mr X is a bad score for the detectives, and vice versa.
     * @param alternativeMoves Alternative moves that could've been made instead of {@code move}. This can be used to improve the scoring - for example
     *                         if a double move was used when a single can reach the same destination, the double move is given a worse score as it's more wasteful
     * @param move             The move to score
     * @param board            The board, which should be the result of {@link GameState#advance(Move)} with {@code move} as its argument.
     *                         In other words, the result of making the given move
     * @return A score for the move, which may be {@link  Double#POSITIVE_INFINITY} or {@link Double#NEGATIVE_INFINITY} to indicate winning or losing, respectively
     */
    default double calculateScore(boolean isMrX, Collection<Move> alternativeMoves, Move move, Board board) {
        if (isMrX) {
            return calculateMrXScore(alternativeMoves, move, board);
        }
        return calculateDetectiveScore(alternativeMoves, move, board);
    }

    /**
     * Calculate a score for a move as Mr X
     *
     * @see MoveScorer#calculateScore(boolean, Collection, Move, Board)
     */
    double calculateMrXScore(Collection<Move> availableMoves, Move move, Board board);

    /**
     * Calculate a score for a move as a Detective
     *
     * @see MoveScorer#calculateScore(boolean, Collection, Move, Board)
     */
    double calculateDetectiveScore(Collection<Move> availableMoves, Move move, Board board);
}
