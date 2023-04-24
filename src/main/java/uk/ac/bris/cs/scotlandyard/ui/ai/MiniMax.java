package uk.ac.bris.cs.scotlandyard.ui.ai;

import javafx.util.Pair;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Piece;

import javax.annotation.Nullable;

public class MiniMax {
    private final Board board;

    public MiniMax(Board board) {
        this.board = board;

    }

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

    //returns a pair of the move destination and the distance
    public Pair<Move, Double> minimax(@Nullable MoveTree.Node node, MoveTree moveTree, int depth) {

        boolean isMrX = node == null ? playerChecker(moveTree) : node.move().commencedBy().isMrX();
        //base case:
        if (depth <= 0 || moveTree.getChildren().isEmpty()) {
            if (node == null) {
                throw new IllegalArgumentException("Finished traversing but there's no node. Empty tree?");
            }
            if (isMrX) {
                return new Pair<>(node.move(), Dijkstra.dijkstraScore(MoveTree.getDetectiveDistances(this.board, moveTree.getSource())));
            } else {
                return new Pair<>(node.move(), MoveTree.getMrXDistance(this.board, moveTree.getSource()).doubleValue());
            }
        }

        Pair<Move, Double> evalPair = null;

        if (isMrX) { //maximising player, thus maximise the minimum distance
            double maxEval = Double.NEGATIVE_INFINITY;
            for (MoveTree.Node subNode : moveTree.getChildren()) {
                evalPair = minimax(subNode, subNode.child(), depth - 1);
                maxEval = Double.max(maxEval, evalPair.getValue());
            }
            return new Pair<>(evalPair.getKey(), maxEval);

        } else { //detective, thus minimise the maximum distance
            double minEval = Double.NEGATIVE_INFINITY;
            for (MoveTree.Node subNode : moveTree.getChildren()) {
                evalPair = minimax(subNode, subNode.child(), depth - 1);
                minEval = Double.min(minEval, evalPair.getValue());

            }
            return new Pair<>(evalPair.getKey(), minEval);
        }
        //TODO use the value at the shallowest depth not from the bottom
    }

}
