package uk.ac.bris.cs.scotlandyard.ui.ai;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class ConsListTest extends TestCase {

    @Test
    public void testList() {
        ConsList<Integer> i =
                ConsList.<Integer>empty()
                        .prepend(3)
                        .prepend(2)
                        .prepend(1);

        assertEquals(i.head(), Optional.of(1));
        assertEquals(i.last(), Optional.of(3));
    }


    @Test
    public void testToFromList() {
        for (int i = 0; i < 10; i++) {
            List<Integer> l = new ArrayList<>(IntStream.range(1, 20).boxed().toList());
            Collections.shuffle(l);

            assertEquals(ConsList.fromList(l).asList(), l);
        }


    }
}