package uk.ac.bris.cs.scotlandyard.ui.ai;

import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Ai;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.minimax.GenericMiniMax;
import uk.ac.bris.cs.scotlandyard.ui.ai.minimax.MinimaxFactory;
import uk.ac.bris.cs.scotlandyard.ui.ai.minimax.scoring.DistanceUtil;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MyAi implements Ai {

    private GenericMiniMax miniMax;

    private int previousMrXLocation;

    @Nonnull
    @Override
    public String name() {
        return "Dijkstra-tron";
    }

    @Override
    public void onStart() {
        MinimaxFactory minimaxFactory = new MinimaxFactory();
        miniMax = minimaxFactory.createWithPruning(true);
    }

    @Nonnull
    @Override
    public Move pickMove(
            @Nonnull Board board,
            Pair<Long, TimeUnit> timeoutPair) {
        try {
            return CompletableFuture.supplyAsync(
                            () -> pickGoodMove(board))
                    .get(timeoutPair.left() - 1, timeoutPair.right()); //gives enough time to make random move
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
        Optional<Move> anyMrXMove = board.getAvailableMoves()
                .stream()
                .filter(m -> m.commencedBy().isMrX())
                .findAny();
        var isMrX = anyMrXMove.isPresent();
        anyMrXMove.ifPresent(loc -> previousMrXLocation = loc.source());

        boolean doubleMoveAvailable = board.getAvailableMoves()
                .stream()
                .anyMatch(MoveUtil::checkDoubleMove);

        double currentPositionScore = Dijkstra.dijkstraScore(DistanceUtil.getDetectiveDistances(board, previousMrXLocation)); //an evaluation of the current position
        boolean allowDoubleMove = currentPositionScore < 2 && doubleMoveAvailable; //double move will occur in situations where detectives are less than a node away (mostly)

        int depth = allowDoubleMove ? 3 : 5; // depth smaller for doubleMoves to avoid timeout from a large tree


        GenericMiniMax.MinimaxResult result = miniMax.minimaxRoot(isMrX, (Board.GameState) board, depth, previousMrXLocation, allowDoubleMove);
        return result.move();
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
