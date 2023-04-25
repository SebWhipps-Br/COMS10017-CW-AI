package uk.ac.bris.cs.scotlandyard.ui.ai;

import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Ai;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Piece;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
        try {
            return CompletableFuture.supplyAsync(
                    () -> pickGoodMove(board))
                    .get(timeoutPair.left() - 1, timeoutPair.right());
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            System.out.println("Move timout : picking random move");
            return pickRandomMove(board);
        }
    }
        private Move pickGoodMove(Board board){
        int mrXLocation = board.getAvailableMoves()
                .stream()
                .filter(m -> m.commencedBy().isMrX())
                .findFirst().orElseThrow().source();
        boolean doubleMoveAvailable = board.getAvailableMoves().stream()
                .anyMatch(MyAi::checkDoubleMove);

        double currentPositionScore = Dijkstra.dijkstraScore(MoveTree.getDetectiveDistances(board, mrXLocation));

        boolean allowDoubleMove = currentPositionScore < 2 && doubleMoveAvailable;

        //dijkstra for each possible move
        int depth = allowDoubleMove ? 4 : 8;
        MoveTree tree = MoveTree.generateRootTree((Board.GameState) board, depth, allowDoubleMove);
        System.out.println("1");
        MiniMax miniMax = new MiniMax();
        System.out.println("2");
         // all the moves should start at the same position

        System.out.println("tree size" + tree.size());
        return miniMax.minimax(tree, depth, board, mrXLocation).move();
    }

    private Move pickRandomMove(Board board){
        var moves = board.getAvailableMoves().asList();
        return moves.get(new Random().nextInt(moves.size()));
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


}
