package uk.ac.bris.cs.scotlandyard.ui.ai;

import javafx.util.Pair;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.ImmutableBoard;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Piece;

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


    public Move minimax(MoveTree root, int depth, Board board, int mrXLocation) {
        if (root.getChildren().isEmpty()) {
            throw new IllegalArgumentException("Empty tree");
        }
        if (!playerChecker(root)) {
            throw new IllegalArgumentException("Not Mr X");
        }

        Pair<Move, Double> max = null;
        for (MoveTree.Node child : root.getChildren()) {
            Pair<Move, Double> minimax = minimax(child, depth - 1, ((Board.GameState) board).advance(child.move()), mrXLocation);
            System.out.println(minimax);
            if (max == null || minimax.getValue() > max.getValue()) {
                max = new Pair<>(child.move(), minimax.getValue());
            }
        }
        if (max == null) {
            throw new IllegalArgumentException("No children");
        }
        return max.getKey();
    }

    //returns a pair of the move destination and the distance
    public Pair<Move, Double> minimax(MoveTree.Node node, int depth, Board board, int mrXLocation) {
        boolean isMrX = node.move().commencedBy().isMrX();

        //base case:
        if (depth <= 0 || node.child().getChildren().isEmpty()) {
            if (isMrX) {
                return new Pair<>(node.move(), Dijkstra.dijkstraScore(MoveTree.getDetectiveDistances(board, MoveUtil.moveDestination(node.move()))));
            } else {
                if (board.getAvailableMoves().isEmpty()) { // detectives have won
                    return new Pair<>(node.move(), Double.POSITIVE_INFINITY); // so very high score
                }
                return new Pair<>(node.move(), MoveTree.getMrXDistance(board, mrXLocation, MoveUtil.moveDestination(node.move())).doubleValue());
            }
        }

        Pair<Move, Double> evalPair = null; // holds move and the evaluation

        if (isMrX) { //maximising player, thus maximise the minimum distance
            double maxEval = Double.NEGATIVE_INFINITY;
            for (MoveTree.Node subNode : node.child().getChildren()) {
                evalPair = minimax(subNode, depth - 1, ((Board.GameState) board).advance(subNode.move()), subNode.move().accept(MoveUtil.destinationChecker));
                maxEval = Double.max(maxEval, evalPair.getValue());
            }
            return new Pair<>(node.move(), maxEval);

        } else { //detective, thus minimise the maximum distance
            double minEval = Double.POSITIVE_INFINITY;
            for (MoveTree.Node subNode : node.child().getChildren()) {
                evalPair = minimax(subNode, depth - 1, ((Board.GameState) board).advance(subNode.move()), mrXLocation);
                minEval = Double.min(minEval, evalPair.getValue());
            }
            return new Pair<>(node.move(), minEval);
        }
    }
}
