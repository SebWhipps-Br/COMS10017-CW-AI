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

        //dijkstra for each possible move
        int depth = 3;
        MoveTree tree = MoveTree.generate((Board.GameState) board, depth);
        System.out.println("1");
        MiniMax miniMax = new MiniMax();
        System.out.println("2");
        int mrXLocation = board.getAvailableMoves()
                .stream()
                .filter(m -> m.commencedBy().isMrX())
                .findAny().orElseThrow().source(); // all the moves should start at the same position

        var move = miniMax.minimax(tree, depth, board, mrXLocation);
        return move.move();
    }


}
