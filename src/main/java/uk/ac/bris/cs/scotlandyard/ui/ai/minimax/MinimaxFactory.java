package uk.ac.bris.cs.scotlandyard.ui.ai.minimax;

import uk.ac.bris.cs.scotlandyard.ui.ai.minimax.scoring.DijkstraMoveScorer;

public class MinimaxFactory {
    public GenericMiniMax createWithPruning(boolean caching) {
        return new AlphaBetaMinimax(new DijkstraMoveScorer(), caching);
    }

    public GenericMiniMax create(boolean caching) {
        return new Minimax(new DijkstraMoveScorer(), caching);
    }
}
