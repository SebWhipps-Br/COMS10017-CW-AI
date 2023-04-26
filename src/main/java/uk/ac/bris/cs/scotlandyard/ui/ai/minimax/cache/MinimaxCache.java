package uk.ac.bris.cs.scotlandyard.ui.ai.minimax.cache;

import uk.ac.bris.cs.scotlandyard.ui.ai.minimax.GenericMiniMax;

import javax.annotation.Nullable;

public interface MinimaxCache<S> {

    @Nullable
    GenericMiniMax.MinimaxResult get(S input);

    void put(S key, GenericMiniMax.MinimaxResult val);

}
