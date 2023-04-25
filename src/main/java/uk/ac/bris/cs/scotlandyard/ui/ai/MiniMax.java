package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Piece;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.stream.StreamSupport;

public class MiniMax {


    private double calculateScore(boolean isMrX, Collection<Move> availableMoves, Move move, Board board) {
        if (isMrX) {
            if (board.getWinner().stream().anyMatch(Piece::isDetective)) { // if the detectives win
                return Double.NEGATIVE_INFINITY; // that's very bad for mr x!
            }
            if (board.getWinner().contains(Piece.MrX.MRX)) { // if mr x wins
                return Double.POSITIVE_INFINITY; // that's very good for him
            }
            return MoveTree.getMoveScore(board, availableMoves, move);
        } else {
            if (board.getWinner().contains(Piece.MrX.MRX)) {// if mr x wins
                return Double.NEGATIVE_INFINITY; // very bad score for detectives
            }
            if (board.getWinner().stream().anyMatch(Piece::isDetective)) { // if the detectives win
                return Double.POSITIVE_INFINITY; // that's very good for the detectives
            }
            double score = MoveTree.getMoveScore(board, availableMoves, move);
            return -score; // The detectives want to get as close to Mr x as possible, so a higher score (further away) is worse for them
        }
    }

    private int getMrXLocationAfter(int currentMrXLocation, Move move) {
        if (move.commencedBy().isMrX()) {
            return MoveUtil.moveDestination(move);
        }
        return currentMrXLocation;
    }

    public MinimaxResult minimaxRoot(boolean isMrX, GameState root, int depth, int mrXLocation, boolean allowDoubleMoves) {
        ImmutableSet<Move> availableMoves = root.getAvailableMoves();
        if (availableMoves.isEmpty()) {
            throw new IllegalArgumentException("No available moves");
        }


        // We are Mr X, so we want to maximise our first move
        return minimax(isMrX, availableMoves, null, root, depth, mrXLocation, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, ConsList.empty(), allowDoubleMoves);
    }


    /**
     * run the minimax algorithm on a move
     *
     * @param alternativeMoves alternative moves that could've been made instead of {@code move}, i.e. adjacent nodes in the "tree". may be empty, used to provide better scoring
     * @param move             the move that is going to be made. if null, it will just try every move in {@code alternativeMoves}
     * @param node             the game state assuming that {@code move} has just been {@link GameState#advance(Move)}'d
     * @param depth            max depth to use
     * @param mrXLocation      the current location of Mr X. We have to carry this around manually as we can't get it from the board without casting to implementation classes
     * @param alpha            current alpha value
     * @param beta             current beta value
     * @return a pair of move and score evaluating the effectiveness of this move
     */
    public MinimaxResult minimax(boolean isMrX, Collection<Move> alternativeMoves, @Nullable Move move, GameState node, int depth, int mrXLocation, double alpha, double beta, ConsList<Move> moves, boolean allowDoubleMoves) {
        //base case:
        if (depth <= 0 || node.getAvailableMoves().isEmpty()) {
            if (move == null) {
                throw new IllegalArgumentException("Move is null but depth <= 0 or no available moves! Impossible game?");
            }
            Move head = moves.last().orElseThrow();
            return new MinimaxResult(head, calculateScore(head.commencedBy().isMrX(), alternativeMoves, head, node));
        }

        Collection<Move> availableMoves = node.getAvailableMoves()
                .stream()
                .filter(m -> StreamSupport.stream(m.tickets().spliterator(), false).allMatch(t -> t != ScotlandYard.Ticket.SECRET)) // TODO be smarter about secret ticket usage
                .filter(m -> !MoveUtil.checkDoubleMove(m) || allowDoubleMoves) //removes doubles if needed
                .toList();

        if (isMrX && availableMoves.stream().allMatch(m -> m.commencedBy().isDetective())) {
            throw new IllegalArgumentException("isMrX = true but only available moves are detective ones");
        }
        if (!isMrX && availableMoves.stream().allMatch(m -> m.commencedBy().isMrX())) {
            throw new IllegalArgumentException("isMrX = false but only available moves are mr x ones");
        }


        if (isMrX) { //maximising player, thus maximise the minimum distance
            return maximise(node, depth, mrXLocation, alpha, beta, moves, allowDoubleMoves, availableMoves);
        } else { //detective, thus minimise the maximum distance
            return minimise(node, depth, mrXLocation, alpha, beta, moves, allowDoubleMoves, availableMoves);
        }
    }

    private MinimaxResult minimise(GameState node, int depth, int mrXLocation, double alpha, double beta, ConsList<Move> moves, boolean allowDoubleMoves, Collection<Move> availableMoves) {
        MinimaxResult value = null;
        for (Move subMove : availableMoves) {
            GameState nextBoard = node.advance(subMove);
            boolean isMrXNow = nextBoard.getAvailableMoves().stream().anyMatch(m -> m.commencedBy().isMrX()); // there are multiple detectives, so we can't just assume it will be mr x's turn afterwards

            var res = minimax(isMrXNow, availableMoves, subMove, nextBoard, depth - 1, getMrXLocationAfter(mrXLocation, subMove), alpha, beta, moves.prepend(subMove), allowDoubleMoves);
            value = value == null ? res : min(value, res);
            beta = Math.min(beta, value.score());

            if (value.score() <= alpha) {
                break; // alpha cutoff
            }
        }
        return value;
    }

    private MinimaxResult maximise(GameState node, int depth, int mrXLocation, double alpha, double beta, ConsList<Move> moves, boolean allowDoubleMoves, Collection<Move> availableMoves) {
        MinimaxResult value = null;
        for (Move subMove : availableMoves) {
            GameState nextBoard = node.advance(subMove);

            var res = minimax(false, availableMoves, subMove, nextBoard, depth - 1, getMrXLocationAfter(mrXLocation, subMove), alpha, beta, moves.prepend(subMove), allowDoubleMoves);
            value = value == null ? res : max(value, res);
            alpha = Math.max(alpha, value.score());

            if (value.score() >= beta) {
                break; // beta cutoff
            }
        }
        return value;
    }

    private <T extends Comparable<T>> T max(T a, T b) {
        return (a.compareTo(b) >= 0) ? a : b;
    }

    private <T extends Comparable<T>> T min(T a, T b) {
        return (a.compareTo(b) <= 0) ? a : b;
    }


    // Stores a move and score
    // Slightly more efficient than using a Pair as it avoids boxing the Double
    record MinimaxResult(Move move, double score) implements Comparable<MinimaxResult> {
        @Override
        public int compareTo(MinimaxResult o) {
            return Double.compare(this.score, o.score);
        }
    }
}
