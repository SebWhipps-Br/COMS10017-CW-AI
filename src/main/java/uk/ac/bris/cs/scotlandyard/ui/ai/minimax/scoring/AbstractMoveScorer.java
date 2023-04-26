package uk.ac.bris.cs.scotlandyard.ui.ai.minimax.scoring;

import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Piece;

import java.util.Collection;

/**
 * Abstract subclass of {@link MoveScorer} that handles the winning / losing laws,
 * leaving the other possibilities up to the subtype
 */
public abstract class AbstractMoveScorer implements MoveScorer {

    @Override
    public final double calculateMrXScore(Collection<Move> availableMoves, Move move, Board board) {
        if (board.getWinner().stream().anyMatch(Piece::isDetective)) { // if the detectives win
            return Double.NEGATIVE_INFINITY; // that's very bad for mr x!
        }
        if (board.getWinner().contains(Piece.MrX.MRX)) { // if mr x wins
            return Double.POSITIVE_INFINITY; // that's very good for him
        }
        return calculateMrXScore0(availableMoves, move, board);
    }

    @Override
    public final double calculateDetectiveScore(Collection<Move> availableMoves, Move move, Board board) {
        if (board.getWinner().contains(Piece.MrX.MRX)) {// if mr x wins
            return Double.NEGATIVE_INFINITY; // very bad score for detectives
        }
        if (board.getWinner().stream().anyMatch(Piece::isDetective)) { // if the detectives win
            return Double.POSITIVE_INFINITY; // that's very good for the detectives
        }
        return calculateDetectiveScore0(availableMoves, move, board);
    }

    protected abstract double calculateMrXScore0(Collection<Move> availableMoves, Move move, Board board);

    protected abstract double calculateDetectiveScore0(Collection<Move> availableMoves, Move move, Board board);
}
