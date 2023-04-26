package uk.ac.bris.cs.scotlandyard.ui.ai.minimax;

import uk.ac.bris.cs.scotlandyard.ui.ai.minimax.scoring.DijkstraMoveScorer;

public class MinimaxFactory {
    public GenericMiniMax createStandard(boolean caching) {
        return new AlphaBetaMinimax(new DijkstraMoveScorer(), caching);
    }
}
