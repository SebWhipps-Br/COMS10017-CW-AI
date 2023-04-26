package uk.ac.bris.cs.scotlandyard.ui.ai.minimax.cache;

import uk.ac.bris.cs.scotlandyard.ui.ai.minimax.GenericMiniMax;

/**
 * A {@link MinimaxCache} that doesn't actually cache anything
 */
public class VoidMinimaxCache<S> implements MinimaxCache<S> {
    @Override
    public GenericMiniMax.MinimaxResult get(S input) {
        return null;
    }

    @Override
    public void put(S key, GenericMiniMax.MinimaxResult val) {
// do nothing
    }
}
