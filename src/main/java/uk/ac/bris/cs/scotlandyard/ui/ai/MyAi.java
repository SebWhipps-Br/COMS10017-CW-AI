package uk.ac.bris.cs.scotlandyard.ui.ai;

import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Ai;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;

import javax.annotation.Nonnull;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MyAi implements Ai {

    @Nonnull
    @Override
    public String name() {
        return "Dijkstra-tron";
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

    /**
     * takes a given board and uses a gameTree, Dijkstra's shortest path and a MiniMax to pick a good move
     *
     * @param board for the given gameState
     * @return a move
     */
    private Move pickGoodMove(Board board) {
        int mrXLocation = board.getAvailableMoves()
                .stream()
                .filter(m -> m.commencedBy().isMrX())
                .findFirst().orElseThrow().source();
        boolean doubleMoveAvailable = board.getAvailableMoves().stream()
                .anyMatch(MoveUtil::checkDoubleMove);

        double currentPositionScore = Dijkstra.dijkstraScore(MoveTree.getDetectiveDistances(board, mrXLocation)); //an evaluation of the current position
        boolean allowDoubleMove = currentPositionScore < 2 && doubleMoveAvailable; //double move will occur in situations where detectives are less than a node away (mostly)

        int depth = allowDoubleMove ? 4 : 8; // depth is dependent on doubles to avoid timout from a large tree
        MiniMax miniMax = new MiniMax();
        return miniMax.minimaxRoot(true, (Board.GameState) board, depth, mrXLocation, allowDoubleMove).move();
    }

    /**
     * random move generator
     *
     * @param board for the given gameState
     * @return move
     */
    private Move pickRandomMove(Board board) {
        var moves = board.getAvailableMoves().asList();
        return moves.get(new Random().nextInt(moves.size()));
    }
}
