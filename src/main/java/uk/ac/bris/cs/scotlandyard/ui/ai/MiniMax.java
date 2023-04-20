package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Piece;

public class MiniMax {
    public static Double minimax(MoveTree moveTree, int depth){
        boolean isMrX = playerChecker(moveTree);
        //base case:
        if (depth <= 0){
            if (isMrX){
                //
            } else {
                //
            }
        }


        double eval;

        double maxEval = Double.NEGATIVE_INFINITY;
        if (isMrX) { //maximising player, thus maximise the minimum distance
            for (MoveTree.Node subNode : moveTree.getChildren()){
                eval = minimax(subNode.getChild(), depth-1);
                maxEval = Double.max(maxEval,eval);
                return maxEval;
            }
        } else { //detective, thus maximise the minimum distance
            for (MoveTree.Node subNode : moveTree.getChildren()){
                eval = minimax(subNode.getChild(), depth-1);
                maxEval = Double.max(maxEval,eval);
                return maxEval;
            }
        }


        return 0.0;
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
