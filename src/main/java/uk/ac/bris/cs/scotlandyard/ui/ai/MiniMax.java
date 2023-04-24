package uk.ac.bris.cs.scotlandyard.ui.ai;

import javafx.util.Pair;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Piece;

import javax.annotation.Nullable;

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

    //returns a pair of the move destination and the distance
    public Pair<Move, Double> minimax(@Nullable MoveTree.Node node, MoveTree moveTree, int depth, Board board) {
        boolean returnTopFlag = false;
        boolean isMrX = node == null ? playerChecker(moveTree) : node.move().commencedBy().isMrX();
        //base case:
        if (depth <= 0 || moveTree.getChildren().isEmpty()) {
            if (node == null) {
                throw new IllegalArgumentException("Finished traversing but there's no node. Empty tree?");
            }
            if (isMrX) {
                return new Pair<>(node.move(), Dijkstra.dijkstraScore(MoveTree.getDetectiveDistances(board, moveTree.getSource())));
            } else {
                System.out.println("x: " + MoveTree.getMrXDistance(board, moveTree.getSource()));
                return new Pair<>(node.move(), MoveTree.getMrXDistance(board, moveTree.getSource()).doubleValue());
            }

        } else if (node == null) { //the top of the tree, so the first move in the branch is returned
            returnTopFlag = true;
        }


        Pair<Move, Double> evalPair = null; // holds move and the evaluation

        if (isMrX) { //maximising player, thus maximise the minimum distance
            double maxEval = Double.NEGATIVE_INFINITY;
            for (MoveTree.Node subNode : moveTree.getChildren()) {
                evalPair = minimax(subNode, subNode.child(), depth - 1, ((Board.GameState) board).advance(subNode.move()));
                maxEval = Double.max(maxEval, evalPair.getValue());
            }
            if (returnTopFlag){
                return new Pair<>(evalPair.getKey(), maxEval);
            } else {
                return new Pair<>(node.move(), maxEval);
            }

        } else { //detective, thus minimise the maximum distance
            double minEval = Double.POSITIVE_INFINITY;
            for (MoveTree.Node subNode : moveTree.getChildren()) {
                evalPair = minimax(subNode, subNode.child(), depth - 1, ((Board.GameState) board).advance(subNode.move()));
                minEval = Double.min(minEval, evalPair.getValue());

            }
            if (returnTopFlag){
                return new Pair<>(evalPair.getKey(), minEval);
            } else {
                return new Pair<>(node.move(), minEval);
            }
        }
    }
}
