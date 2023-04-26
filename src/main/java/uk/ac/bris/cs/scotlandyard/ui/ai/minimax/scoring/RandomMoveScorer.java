package uk.ac.bris.cs.scotlandyard.ui.ai.minimax.scoring;

import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;

import java.util.Collection;
import java.util.Random;

/**
 * A {@link MoveScorer} that uses random numbers, could be useful for testing
 */
public class RandomMoveScorer extends AbstractMoveScorer {
    private final Random random = new Random();

    @Override
    protected double calculateMrXScore0(Collection<Move> availableMoves, Move move, Board board) {
        return random.nextDouble();
    }

    @Override
    protected double calculateDetectiveScore0(Collection<Move> availableMoves, Move move, Board board) {
        return random.nextDouble();
    }
}
