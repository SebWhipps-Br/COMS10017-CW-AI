package uk.ac.bris.cs.scotlandyard.ui.ai;

import javafx.util.Pair;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Piece;

import java.util.Map;

public class MiniMax {
    private final Board board;

    public MiniMax(Board board) {
        this.board = board;

    }

    //returns a pair of the move destination and the distance
    public Pair<Integer, Double> minimax(MoveTree moveTree, int depth){
        boolean isMrX = playerChecker(moveTree);
        //base case:
        if (depth <= 0 || moveTree.getChildren().isEmpty()){
            if (isMrX){
                return new Pair<>(moveTree.getSource(), Dijkstra.dijkstraScore( MoveTree.getDetectiveDistances(this.board, moveTree.getSource())));
            } else {
                return new Pair<>(moveTree.getSource(), MoveTree.getMrXDistance(this.board, moveTree.getSource()).doubleValue());
            }
        }

        Pair<Integer, Double> evalPair = null;

        if (isMrX) { //maximising player, thus maximise the minimum distance
            double maxEval = Double.NEGATIVE_INFINITY;
            for (MoveTree.Node subNode : moveTree.getChildren()){
                evalPair = minimax(subNode.getChild(), depth-1);
                maxEval = Double.max(maxEval,evalPair.getValue());
            }
            return new Pair<>(evalPair.getKey(), maxEval);

        } else { //detective, thus minimise the maximum distance
            double minEval = Double.NEGATIVE_INFINITY;
            for (MoveTree.Node subNode : moveTree.getChildren()){
                evalPair = minimax(subNode.getChild(), depth-1);
                minEval = Double.min(minEval,evalPair.getValue());

            }
            return new Pair<>(evalPair.getKey(), minEval);
        }
        //TODO use the value at the shallowest depth not from the bottom
    }

    private static boolean playerChecker(MoveTree moveTree){
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
        return moveTree.getChildren().stream().map(x -> x.getMove().accept(pieceChecker)).findFirst().orElseThrow().isMrX();
    }

}
