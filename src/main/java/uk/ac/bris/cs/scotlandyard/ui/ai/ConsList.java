package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.*;

/**
 * Fast, immutable singly-linked list
 * This might be seen as un-idiomatic as it's more of a Haskell idea, but it's very useful in the minimax
 * algorithm to create a backtracking list - mutable lists don't work very well, guava ImmutableList doesn't perform as well
 *
 * @param <T>
 */
public sealed class ConsList<T> {
    public static <T> ConsList<T> empty() {
        return new Nil<>();
    }

    public static <T> ConsList<T> fromList(Collection<T> t) {
        ConsList<T> l = empty();
        for (T t1 : t) {
            l = l.prepend(t1);
        }
        return l;
    }

    public boolean isEmpty() {
        if (this instanceof ConsList.Nil<T>) {
            return true;
        }
        return false;
    }

    public ConsList<T> prepend(T t) {
        return new Cons<>(t, this);
    }

    public Optional<T> head() {
        if (this instanceof ConsList.Cons<T> cons) {
            return Optional.of(cons.head);
        }
        return Optional.empty();
    }

    public Optional<T> last() {
        if (this instanceof ConsList.Nil<T>) {
            return Optional.empty();
        }
        if (this instanceof ConsList.Cons<T> cons) {
            if (cons.tail.equals(empty())) {
                return Optional.of(cons.head);
            }
            return cons.tail.last();
        }
        return Optional.empty();
    }

    public Optional<ConsList<T>> tail() {
        if (this instanceof ConsList.Cons<T> cons) {
            return Optional.of(cons.tail);
        }
        return Optional.empty();
    }

    public List<T> asList() {
        if (this instanceof ConsList.Nil<T>) {
            return List.of();
        }
        var ll = new LinkedList<T>();
        var l = this;
        while (l instanceof ConsList.Cons<T> cons) {
            ll.addFirst(cons.head);
            l = cons.tail;
        }
        return ll;
    }

    @Override
    public String toString() {
        if (this instanceof ConsList.Nil<T>) {
            return "[]";
        }
        Cons<T> cons = (Cons<T>) this;
        return cons.head + ":" + cons.tail;
    }

    private static final class Nil<T> extends ConsList<T> {
        @Override
        public boolean equals(Object obj) {
            return obj instanceof ConsList.Nil<?>;
        }
    }

    private static final class Cons<T> extends ConsList<T> {
        private final T head;
        private final ConsList<T> tail;

        public Cons(T head, ConsList<T> tail) {
            this.head = head;
            this.tail = tail;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Cons<?> cons = (Cons<?>) o;
            return Objects.equals(head, cons.head) && Objects.equals(tail, cons.tail);
        }

        @Override
        public int hashCode() {
            return Objects.hash(head, tail);
        }
    }
}
