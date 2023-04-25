package uk.ac.bris.cs.scotlandyard.ui.ai;

import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Ai;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Piece;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class MyAi implements Ai {


    private static Set<Integer> getDetectiveLocations(Board board) {
        Set<Integer> detectiveLocations = new HashSet<>();
        for (Piece piece : board.getPlayers()) {
            if (piece.isDetective()) {
                detectiveLocations.add(board.getDetectiveLocation((Piece.Detective) piece).get());
            }
        }
        return detectiveLocations;
    }

    public static boolean checkDoubleMove(Move move) {
        Move.Visitor<Boolean> doubleMoveChecker = new Move.Visitor<>() {
            @Override
            public Boolean visit(Move.SingleMove move) {
                return false;
            }

            @Override
            public Boolean visit(Move.DoubleMove move) {
                return true;
            }
        };
        return move.accept(doubleMoveChecker);
    }

    @Nonnull
    @Override
    public String name() {
        return "Dijkstratron";
    }

    @Nonnull
    @Override
    public Move pickMove(
            @Nonnull Board board,
            Pair<Long, TimeUnit> timeoutPair) {
        //TODO pre evaluate the position
        // if the position is poor consider and we have double moves -> doubles and reduce depth

        int mrXLocation = board.getAvailableMoves()
                .stream()
                .filter(m -> m.commencedBy().isMrX())
                .findFirst().orElseThrow().source();
        boolean doubleMoveAvailible = board.getAvailableMoves().stream()
                .anyMatch(MyAi::checkDoubleMove);

        double currentPositionScore = Dijkstra.dijkstraScore(MoveTree.getDetectiveDistances(board, mrXLocation));

        boolean allowDoubleMove = false;
        if (currentPositionScore < 2 && doubleMoveAvailible) {
            allowDoubleMove = true;
        }
        //
        //dijkstra for each possible move
        int depth = 10;
        System.out.println("1");
        MiniMax miniMax = new MiniMax();
        System.out.println("2");
        // all the moves should start at the same position

        System.out.println(board.getAvailableMoves());
        MiniMax.MinimaxResult minimaxResult = miniMax.minimaxRoot((Board.GameState) board, depth, mrXLocation);
        System.out.println(minimaxResult);
        return minimaxResult.move();
    }


}
