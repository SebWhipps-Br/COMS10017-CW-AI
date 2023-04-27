package uk.ac.bris.cs.scotlandyard.ui.ai.minimax;

import com.esotericsoftware.kryo.util.Null;
import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.ComparableUtil;
import uk.ac.bris.cs.scotlandyard.ui.ai.MoveUtil;
import uk.ac.bris.cs.scotlandyard.ui.ai.minimax.cache.HashingMinimaxCache;
import uk.ac.bris.cs.scotlandyard.ui.ai.minimax.cache.MinimaxCache;
import uk.ac.bris.cs.scotlandyard.ui.ai.minimax.cache.VoidMinimaxCache;
import uk.ac.bris.cs.scotlandyard.ui.ai.minimax.scoring.MoveScorer;

import javax.annotation.Nullable;
import java.util.Collection;

public class AlphaBetaMinimax implements GenericMiniMax {

    private final MinimaxCache<MinimaxState, FullMinimaxResult> cache;
    private final MoveScorer moveScorer;

    public AlphaBetaMinimax(MoveScorer moveScorer, boolean cache) {
        this.moveScorer = moveScorer;
        this.cache = cache ? new HashingMinimaxCache<>() : new VoidMinimaxCache<>();
    }


    public MinimaxResult minimaxRoot(boolean isMrX, GameState root, int depth, int mrXLocation, boolean allowDoubleMoves) {
        ImmutableSet<Move> availableMoves = root.getAvailableMoves();
        if (availableMoves.isEmpty()) {
            throw new IllegalArgumentException("No available moves");
        }


        return minimax(isMrX, availableMoves, null, root, depth, mrXLocation, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, null, allowDoubleMoves);
    }

    /**
     * run the minimax algorithm on a move
     *
     * @param alternativeMoves alternative moves that could've been made instead of {@code move}, i.e. adjacent nodes in the "tree". may be empty, used to provide better scoring
     * @param move             the move that is going to be made. if null, it will just try every move in {@code alternativeMoves}
     * @param node             the game state assuming that {@code move} has just been {@link GameState#advance(Move)}'d
     * @param depth            max depth to use
     * @param mrXLocation      the current location of Mr X. We have to carry this around manually as we can't get it from the board without casting to implementation classes
     * @param alpha            current alpha value
     * @param beta             current beta value
     * @param head             The "head" node, i.e. the shallowest node in the subtree. This is what's finally returned. Should be null if the tree is being evaluated at the root
     * @param allowDoubleMoves If double moves should be considered
     * @return a pair of move and score evaluating the effectiveness of this move
     */
    public MinimaxResult minimax(boolean isMrX,
                                 Collection<Move> alternativeMoves,
                                 @Nullable Move move,
                                 GameState node,
                                 int depth,
                                 int mrXLocation,
                                 double alpha,
                                 double beta,
                                 @Null Move head,
                                 boolean allowDoubleMoves) {

        //base case:
        ImmutableSet<Move> allAvailableMoves = node.getAvailableMoves();
        Collection<Move> availableMoves = allAvailableMoves
                .stream()
                .filter(m -> !MoveUtil.checkDoubleMove(m) || allowDoubleMoves) //removes doubles if needed
                .toList();

        if (depth <= 0 || availableMoves.isEmpty()) {
            if (move == null) {
                throw new IllegalArgumentException("Move is null but depth <= 0 or no available moves! Impossible game?");
            }
            if (head == null) {
                throw new IllegalArgumentException("head is null");
            }
            return new MinimaxResult(head, moveScorer.calculateScore(head.commencedBy().isMrX(), alternativeMoves, head, node));
        }


        // do not use the filtered available moves here!! it messes up the results
        MinimaxState key = new MinimaxState(alpha, beta, depth, allAvailableMoves, isMrX);

        FullMinimaxResult cachedResult = cache.get(key); // check for cache, if applicable
        if (cachedResult != null && cachedResult.depth >= depth) {
            return cachedResult.res();
        }

        FullMinimaxResult res;
        if (isMrX) { //maximising player, thus maximise the minimum distance
            res = maximise(node, depth, mrXLocation, alpha, beta, head, allowDoubleMoves, availableMoves);
        } else { //detective, thus minimise the maximum distance
            res = minimise(node, depth, mrXLocation, alpha, beta, head, allowDoubleMoves, availableMoves);
        }

        if (alpha < res.res().score() && beta > res.res().score()) {
            cache.put(key, res);
        }

        return res.res();
    }

    private FullMinimaxResult minimise(GameState node, int depth, int mrXLocation, double alpha, double beta, @Nullable Move head, boolean allowDoubleMoves, Collection<Move> availableMoves) {
        MinimaxResult value = null;
        for (Move subMove : availableMoves) {
            GameState nextBoard = node.advance(subMove);
            boolean isMrXNow = nextBoard.getAvailableMoves().stream().anyMatch(m -> m.commencedBy().isMrX()); // there are multiple detectives, so we can't just assume it will be mr x's turn afterwards

            var res = minimax(isMrXNow, availableMoves, subMove, nextBoard, depth - 1, MoveUtil.getMrXLocationAfter(mrXLocation, subMove), alpha, beta, head == null ? subMove : head, allowDoubleMoves);
            value = value == null ? res : ComparableUtil.min(value, res);
            beta = Math.min(beta, value.score());

            if (value.score() <= alpha) {
                break; // alpha cutoff
            }
        }
        return new FullMinimaxResult(value, alpha, beta, depth);
    }

    private FullMinimaxResult maximise(GameState node, int depth, int mrXLocation, double alpha, double beta, @Nullable Move head, boolean allowDoubleMoves, Collection<Move> availableMoves) {
        MinimaxResult value = null;
        for (Move subMove : availableMoves) {
            GameState nextBoard = node.advance(subMove);

            var res = minimax(false, availableMoves, subMove, nextBoard, depth - 1, MoveUtil.getMrXLocationAfter(mrXLocation, subMove), alpha, beta, head == null ? subMove : head, allowDoubleMoves);
            value = value == null ? res : ComparableUtil.max(value, res);
            alpha = Math.max(alpha, value.score());

            if (value.score() >= beta) {
                break; // beta cutoff
            }
        }
        return new FullMinimaxResult(value, alpha, beta, depth);
    }


    private record MinimaxState(double alpha, double beta, int depth, Collection<Move> availableMoves, boolean isMrX) {

    }


    record FullMinimaxResult(MinimaxResult res, double alpha, double beta, double depth) {

    }
}
