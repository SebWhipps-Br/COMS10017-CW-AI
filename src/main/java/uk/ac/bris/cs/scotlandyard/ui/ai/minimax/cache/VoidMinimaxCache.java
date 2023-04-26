package uk.ac.bris.cs.scotlandyard.ui.ai.minimax.cache;

/**
 * A {@link MinimaxCache} that doesn't actually cache anything
 */
public class VoidMinimaxCache<K, V> implements MinimaxCache<K, V> {
    @Override
    public V get(K input) {
        return null;
    }

    @Override
    public void put(K key, V val) {
// do nothing
    }
}
