package uk.ac.bris.cs.scotlandyard.ui.ai.minimax.scoring;

import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.MoveTree;

import java.util.Collection;

/**
 * A {@link MoveScorer} that uses Dijkstra's algorithm to score based on distance to other players
 */
public class DijkstraMoveScorer extends AbstractMoveScorer {


    @Override
    protected double calculateMrXScore0(Collection<Move> availableMoves, Move move, Board board) {
        return MoveTree.getMoveScore(board, availableMoves, move);
    }

    @Override
    protected double calculateDetectiveScore0(Collection<Move> availableMoves, Move move, Board board) {
        double score = MoveTree.getMoveScore(board, availableMoves, move);
        return -score; // The detectives want to get as close to Mr x as possible, so a higher score (further away) is worse for them
    }
}
