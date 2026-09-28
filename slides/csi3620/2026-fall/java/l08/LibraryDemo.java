package l08;

import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

/** The running keys in java.util's ordered collections. */
public final class LibraryDemo {

    private LibraryDemo() {
    }

    static final List<Integer> RUNNING = List.of(50, 30, 70, 20, 40, 80, 35, 45);

    /** Maps each running key to its insertion position, then queries by order. */
    static TreeMap<Integer, String> roster() {
        TreeMap<Integer, String> m = new TreeMap<>();
        for (int i = 0; i < RUNNING.size(); i++) {
            m.put(RUNNING.get(i), "record " + i);
        }
        return m;
    }

    /** Order queries a hash map cannot answer quickly. */
    static List<Integer> queries(TreeMap<Integer, String> m) {
        return List.of(
                m.firstKey(),                     // 20: smallest key
                m.lastKey(),                      // 80: largest key
                m.floorKey(42),                   // 40: largest key <= 42
                m.ceilingKey(42),                 // 45: smallest key >= 42
                m.higherKey(45));                 // 50: successor of 45
    }

    static TreeSet<Integer> keySet() {
        TreeSet<Integer> s = new TreeSet<>(RUNNING);
        s.add(40);                                // duplicate: add returns false
        return s;
    }
}
