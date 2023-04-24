package uk.ac.bris.cs.scotlandyard.ui.ai;

import javafx.util.Pair;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Piece;

import java.util.Comparator;

public class MiniMax {


    private static boolean playerChecker(MoveTree moveTree) {
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
        return moveTree.getChildren().stream().map(x -> x.move().accept(pieceChecker)).findFirst().orElseThrow().isMrX();
    }


    public Pair<Move, Double> minimax(MoveTree root, int depth, Board board, int mrXLocation) {
        if (root.getChildren().isEmpty()) {
            throw new IllegalArgumentException("Empty tree");
        }
        if (!playerChecker(root)) {
            throw new IllegalArgumentException("Not Mr X");
        }

        // We are Mr X, so the child moves are the detectives - we want to minimize the detective results
        return root.getChildren()
                .parallelStream()
                .map(child -> minimax(child, depth - 1, ((Board.GameState) board).advance(child.move()), mrXLocation))
                .min(Comparator.comparingDouble(Pair::getValue))
                .orElseThrow();
    }

    //returns a pair of the move destination and the distance
    public Pair<Move, Double> minimax(MoveTree.Node node, int depth, Board board, int mrXLocation) {
        boolean isMrX = node.move().commencedBy().isMrX();

        //base case:
        if (depth <= 0 || node.child().getChildren().isEmpty()) {
            if (isMrX) {
                Double score = MoveTree.getMoveScore(board, node.move());
                return new Pair<>(node.move(), score);
            } else {
                if (board.getAvailableMoves().isEmpty()) { // detectives have won
                    return new Pair<>(node.move(), Double.MAX_VALUE); // so very high score
                }
                Double score = MoveTree.getMoveScore(board, node.move());
                return new Pair<>(node.move(), score);
            }
        }

        Pair<Move, Double> evalPair = null; // holds move and the evaluation

        if (isMrX) { //maximising player, thus maximise the minimum distance
            return node.child().getChildren()
                    .parallelStream()
                    .map(subNode -> minimax(subNode, depth - 1, ((Board.GameState) board).advance(subNode.move()), subNode.move().accept(MoveUtil.destinationChecker)))
                    .max(Comparator.comparingDouble(Pair::getValue))
                    .map(p -> new Pair<>(node.move(), p.getValue()))
                    .orElseThrow();

        } else { //detective, thus minimise the maximum distance
            return node.child().getChildren()
                    .parallelStream()
                    .map(subNode -> minimax(subNode, depth - 1, ((Board.GameState) board).advance(subNode.move()), mrXLocation))
                    .min(Comparator.comparingDouble(Pair::getValue))
                    .map(p -> new Pair<>(node.move(), p.getValue()))
                    .orElseThrow();
        }
    }
}
