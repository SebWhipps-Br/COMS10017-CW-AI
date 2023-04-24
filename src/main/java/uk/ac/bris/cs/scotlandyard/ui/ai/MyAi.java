package uk.ac.bris.cs.scotlandyard.ui.ai;

import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Ai;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

public class MyAi implements Ai {


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
        int depth = 7   ;
        MoveTree tree = MoveTree.generateRootTree((Board.GameState) board, depth);
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
