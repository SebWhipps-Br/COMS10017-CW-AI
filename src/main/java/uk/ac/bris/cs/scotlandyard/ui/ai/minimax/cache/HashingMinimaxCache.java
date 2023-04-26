package uk.ac.bris.cs.scotlandyard.ui.ai.minimax.cache;

import uk.ac.bris.cs.scotlandyard.ui.ai.minimax.GenericMiniMax;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class HashingMinimaxCache<S> implements MinimaxCache<S> {
    private final Map<S, GenericMiniMax.MinimaxResult> map = new HashMap<>();

    @Override
    public @Nullable GenericMiniMax.MinimaxResult get(S input) {
        return map.get(input);
    }

    @Override
    public void put(S key, GenericMiniMax.MinimaxResult val) {
        map.put(key, val);
    }
}
