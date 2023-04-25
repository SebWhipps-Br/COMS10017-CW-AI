package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Piece;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Comparator;

public class MiniMax {

    private static boolean getCurrentPlayer(MoveTree moveTree) {
        Move.Visitor<Piece> pieceChecker = new Move.Visitor<>() {
            @Override
            public Piece visit(Move.SingleMove move) {
                return move.commencedBy();
            }

            @Override
            public Piece visit(Move.DoubleMove move) {
                return move.commencedBy();
            }
        };
        //returns true if mrX
        return moveTree.getChildren().stream()
                .map(x -> x.move().accept(pieceChecker))
                .findAny()
                .orElseThrow()
                .isMrX();
    }


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

    public MinimaxResult minimaxRoot(Board.GameState root, int depth, int mrXLocation) {
        ImmutableSet<Move> availableMoves = root.getAvailableMoves();
        if (availableMoves.isEmpty()) {
            throw new IllegalArgumentException("No available moves");
        }


        // We are Mr X, so the child moves are the detectives - we want to minimize the detective results
        return maximise(null, root, depth, mrXLocation, availableMoves);
    }


    //returns a pair of the move destination and the distance
    public MinimaxResult minimax(Move move, Board.GameState node, int depth, int mrXLocation) {
        boolean isMrX = move.commencedBy().isMrX();

        Collection<Move> availableMoves = node.getAvailableMoves()
                .stream()
                .filter(isMrX ? m -> m.commencedBy().isMrX() : m -> m.commencedBy().isDetective())
                .toList();


        //base case:
        if (depth <= 0 || availableMoves.isEmpty()) {
            return new MinimaxResult(move, calculateScore(isMrX, availableMoves, move, node));
        }


        if (isMrX) { //maximising player, thus maximise the minimum distance
            return maximise(move, node, depth, mrXLocation, availableMoves);

        } else { //detective, thus minimise the maximum distance
            return availableMoves
                    .parallelStream()
                    .map(subNode -> {
                        Board.GameState nextBoard = node.advance(subNode);
                        return minimax(subNode, nextBoard, depth - 1, getMrXLocationAfter(mrXLocation, subNode));
                    })
                    .min(Comparator.comparingDouble(MinimaxResult::score))
                    .map(p -> new MinimaxResult(move, p.score()))
                    .orElseThrow();
        }
    }

    private MinimaxResult maximise(@Nullable Move move, Board.GameState node, int depth, int mrXLocation, Collection<Move> availableMoves) {
        return availableMoves
                .parallelStream()
                .map(subNode -> {
                    Board.GameState nextBoard = node.advance(subNode);
                    return minimax(subNode, nextBoard, depth - 1, getMrXLocationAfter(mrXLocation, subNode));
                })
                .max(Comparator.comparingDouble(MinimaxResult::score))
                .map(p -> move == null ? p : new MinimaxResult(move, p.score()))
                .orElseThrow();
    }

    // Stores a move and score
    // Slightly more efficient than using a Pair as it avoids boxing the Double
    record MinimaxResult(Move move, double score) {
    }
}
