package uk.ac.bris.cs.scotlandyard.ui.ai;

public final class ComparableUtil {
    private ComparableUtil() {

    }

    public static <T extends Comparable<T>> T max(T a, T b) {
        return (a.compareTo(b) >= 0) ? a : b;
    }

    public static <T extends Comparable<T>> T min(T a, T b) {
        return (a.compareTo(b) <= 0) ? a : b;
    }

}
