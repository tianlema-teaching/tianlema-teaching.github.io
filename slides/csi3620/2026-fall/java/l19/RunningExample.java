package l19;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** The examples traced on the slides. */
public final class RunningExample {
    private RunningExample() { }

    public static List<Interval> intervals() {
        return List.of(new Interval("a", 1, 4), new Interval("b", 3, 5), new Interval("c", 0, 6),
                new Interval("d", 5, 7), new Interval("e", 3, 8), new Interval("f", 5, 9),
                new Interval("g", 6, 10), new Interval("h", 8, 11));
    }

    public static List<Item> items() {
        return List.of(new Item("X", 30, 6), new Item("Y", 20, 5), new Item("Z", 15, 5));
    }

    public static final int CAPACITY = 10;

    public static Map<Character, Long> frequencies() {
        return new TreeMap<>(Map.of('A', 14L, 'B', 10L, 'C', 7L, 'D', 5L, 'E', 3L, 'F', 1L));
    }
}
