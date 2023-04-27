package uk.ac.bris.cs.scotlandyard.ui.ai.minimax.scoring;

import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.Dijkstra;
import uk.ac.bris.cs.scotlandyard.ui.ai.MoveUtil;

import java.util.Collection;

/**
 * A {@link MoveScorer} that uses Dijkstra's algorithm to score based on distance to other players
 */
public class DijkstraMoveScorer extends AbstractMoveScorer {


    private static double getMoveScore(Board board, Collection<Move> alternativeMoves, Move move) {
        int destination = MoveUtil.moveDestination(move);
        double score = Dijkstra.dijkstraScore(DistanceUtil.getDetectiveDistances(board, move));

        if (move instanceof Move.DoubleMove &&
                alternativeMoves.stream() // if we can get to the same destination using a single move
                        .filter(m -> m instanceof Move.SingleMove) // then we should heavily punish using a double move
                        .anyMatch(m -> MoveUtil.moveDestination(m) == destination)) {
            return score / 10;
        }

        return score;
        /*
         TODO: consider double moves, secret tickets, etc.
            also consider his position (eg dont want to get cornered), and if he has to reveal his move on a turn or not
            Also, the current method of doing the min + avg / 10 seems to discourage risky plays, eg getting close to 1
            detective while evading the other 4. we need to weight it more fairly, not sure how
        */
    }

    @Override
    protected double calculateMrXScore0(Collection<Move> availableMoves, Move move, Board board) {
        return getMoveScore(board, availableMoves, move);
    }

    @Override
    protected double calculateDetectiveScore0(Collection<Move> availableMoves, Move move, Board board) {
        double score = getMoveScore(board, availableMoves, move);
        return -score; // The detectives want to get as close to Mr x as possible, so a higher score (further away) is worse for them
    }
}
