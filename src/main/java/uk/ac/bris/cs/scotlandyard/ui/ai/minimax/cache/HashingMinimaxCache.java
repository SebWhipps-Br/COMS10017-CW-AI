package uk.ac.bris.cs.scotlandyard.ui.ai.minimax.cache;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class HashingMinimaxCache<K, V> implements MinimaxCache<K, V> {
    private final Map<K, V> map = new HashMap<>();

    @Override
    public @Nullable V get(K input) {
        return map.get(input);
    }

    @Override
    public void put(K key, V val) {
        map.put(key, val);
    }
}
