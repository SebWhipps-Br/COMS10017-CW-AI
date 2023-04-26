package uk.ac.bris.cs.scotlandyard.ui.ai.minimax;

import com.esotericsoftware.kryo.util.Null;
import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.MoveUtil;
import uk.ac.bris.cs.scotlandyard.ui.ai.minimax.cache.HashingMinimaxCache;
import uk.ac.bris.cs.scotlandyard.ui.ai.minimax.cache.MinimaxCache;
import uk.ac.bris.cs.scotlandyard.ui.ai.minimax.cache.VoidMinimaxCache;
import uk.ac.bris.cs.scotlandyard.ui.ai.minimax.scoring.MoveScorer;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Stream;

public class Minimax implements GenericMiniMax {

    private final MinimaxCache<MinimaxState, MinimaxResult> cache;
    private final MoveScorer moveScorer;

    public Minimax(MoveScorer moveScorer, boolean cache) {
        this.moveScorer = moveScorer;
        this.cache = cache ? new HashingMinimaxCache<>() : new VoidMinimaxCache<>();
    }


    public MinimaxResult minimaxRoot(boolean isMrX, GameState root, int depth, int mrXLocation, boolean allowDoubleMoves) {
        ImmutableSet<Move> availableMoves = root.getAvailableMoves();
        if (availableMoves.isEmpty()) {
            throw new IllegalArgumentException("No available moves");
        }


        // We are Mr X, so we want to maximise our first move
        return minimax(isMrX, availableMoves, null, root, depth, mrXLocation, null, allowDoubleMoves);
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
                                 @Null Move head,
                                 boolean allowDoubleMoves) {

        //base case:
        Collection<Move> availableMoves = node.getAvailableMoves()
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
        MinimaxState key = new MinimaxState(depth, node.getAvailableMoves(), isMrX);

        GenericMiniMax.MinimaxResult cachedResult = cache.get(key); // check for cache, if applicable
        if (cachedResult != null) {
            return cachedResult;
        }

        MinimaxResult result;
        if (isMrX) { //maximising player, thus maximise the minimum distance
            result = maximise(node, depth, mrXLocation, head, allowDoubleMoves, availableMoves);
        } else { //detective, thus minimise the maximum distance
            result = minimise(node, depth, mrXLocation, head, allowDoubleMoves, availableMoves);
        }
        cache.put(key, result);
        return result;

    }

    private MinimaxResult minimise(GameState node, int depth, int mrXLocation, @Nullable Move head, boolean allowDoubleMoves, Collection<Move> availableMoves) {
        return getSubMoves(node, depth, mrXLocation, head, allowDoubleMoves, availableMoves)
                .min(Comparator.naturalOrder())
                .orElseThrow();
    }

    private Stream<MinimaxResult> getSubMoves(GameState node, int depth, int mrXLocation, Move head, boolean allowDoubleMoves, Collection<Move> availableMoves) {
        return availableMoves
                .parallelStream()
                .map(subMove -> {
                    var nextBoard = node.advance(subMove);
                    return minimax(false, availableMoves, subMove, nextBoard, depth - 1, MoveUtil.getMrXLocationAfter(mrXLocation, subMove), head == null ? subMove : head, allowDoubleMoves);
                });
    }

    private MinimaxResult maximise(GameState node, int depth, int mrXLocation, @Nullable Move head, boolean allowDoubleMoves, Collection<Move> availableMoves) {
        return getSubMoves(node, depth, mrXLocation, head, allowDoubleMoves, availableMoves)
                .max(Comparator.naturalOrder())
                .orElseThrow();
    }


    private record MinimaxState(int depth, Collection<Move> availableMoves, boolean isMrX) {

    }


}
