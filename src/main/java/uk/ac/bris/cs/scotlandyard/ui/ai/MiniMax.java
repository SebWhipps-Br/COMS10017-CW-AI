package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Piece;

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

    public MinimaxResult minimax(MoveTree root, int depth, Board board, int mrXLocation) {
        if (root.getChildren().isEmpty()) {
            throw new IllegalArgumentException("Empty tree");
        }
        if (!getCurrentPlayer(root)) {
            throw new IllegalArgumentException("Not Mr X");
        }

        // We are Mr X, so the child moves are the detectives - we want to minimize the detective results
        return root.getChildren()
                .parallelStream()
                .map(child -> minimax(child, depth - 1, ((Board.GameState) board).advance(child.move()), mrXLocation))
                .min(Comparator.comparingDouble(MinimaxResult::score))
                .orElseThrow();
    }

    private double calculateScore(boolean isMrX, Move move, Board board) {
        if (isMrX) {
            if (board.getWinner().stream().anyMatch(Piece::isDetective)) { // if the detectives win
                return Double.NEGATIVE_INFINITY; // that's very bad for mr x!
            }
            if (board.getWinner().contains(Piece.MrX.MRX)) { // if mr x wins
                return Double.POSITIVE_INFINITY; // that's very good for him
            }
            return MoveTree.getMoveScore(board, move);
        } else {
            if (board.getWinner().contains(Piece.MrX.MRX)) {// if mr x wins
                return Double.NEGATIVE_INFINITY; // very bad score for detectives
            }
            if (board.getWinner().stream().anyMatch(Piece::isDetective)) { // if the detectives win
                return Double.POSITIVE_INFINITY; // that's very good for the detectives
            }
            double score = MoveTree.getMoveScore(board, move);
            return -score; // The detectives want to get as close to Mr x as possible, so a higher score (further away) is worse for them
        }
    }

    //returns a pair of the move destination and the distance
    public MinimaxResult minimax(MoveTree.Node node, int depth, Board board, int mrXLocation) {
        boolean isMrX = node.move().commencedBy().isMrX();

        //base case:
        if (depth <= 0 || node.child().getChildren().isEmpty()) {
            return new MinimaxResult(node.move(), calculateScore(isMrX, node.move(), board));
        }


        if (isMrX) { //maximising player, thus maximise the minimum distance
            return node.child().getChildren()
                    .parallelStream()
                    .map(subNode -> minimax(subNode, depth - 1, ((Board.GameState) board).advance(subNode.move()), subNode.move().accept(MoveUtil.destinationChecker)))
                    .max(Comparator.comparingDouble(MinimaxResult::score))
                    .map(p -> new MinimaxResult(node.move(), p.score()))
                    .orElseThrow();

        } else { //detective, thus minimise the maximum distance
            return node.child().getChildren()
                    .parallelStream()
                    .map(subNode -> minimax(subNode, depth - 1, ((Board.GameState) board).advance(subNode.move()), mrXLocation))
                    .min(Comparator.comparingDouble(MinimaxResult::score))
                    .map(p -> new MinimaxResult(node.move(), p.score()))
                    .orElseThrow();
        }
    }

    // Stores a move and score
    // Slightly more efficient than using a Pair as it avoids boxing the Double
    record MinimaxResult(Move move, double score) {
    }
}
