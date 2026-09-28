package l12;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** Concrete checks for every class in l12. Prints "l12 OK" when all pass. */
public final class Checks {

    private Checks() {
    }

    static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    static void eq(Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(message + ": expected " + expected + ", got " + actual);
        }
    }

    public static void main(String[] args) {
        hashFunctions();
        runningExampleChaining();
        runningExampleProbing();
        contract();
        edgeCases();
        randomAgainstHashMap(1);
        randomAgainstHashMap(2);
        randomAgainstHashMap(3);
        System.out.println("l12 OK");
    }

    static void hashFunctions() {
        int[] expected = {5, 2, 6, 4, 2};
        for (int i = 0; i < Trace.KEYS.length; i++) {
            eq(expected[i], Hashing.division(Trace.KEYS[i], 7), "division " + Trace.KEYS[i]);
        }
        int[] mult = {3, 1, 0, 3, 1};
        for (int i = 0; i < Trace.KEYS.length; i++) {
            eq(mult[i], Hashing.multiplication(Trace.KEYS[i], 8), "multiplication " + Trace.KEYS[i]);
        }
        for (int k = 0; k < 1000; k++) {
            int h = Hashing.multiplication(k, 13);
            check(h >= 0 && h < 13, "multiplication in range");
        }
        eq(98262, "cat".hashCode(), "cat hashCode");
        eq(99 * 31 * 31 + 97 * 31 + 116, "cat".hashCode(), "cat by the formula");
        for (String s : new String[] {"", "a", "cat", "hash table", "HashMap", "polygenelubricants"}) {
            eq(s.hashCode(), Hashing.stringHash(s), "stringHash " + s);
        }
        eq(0, "".hashCode(), "empty string hash");
        eq(-1932803762, "HashMap".hashCode(), "negative hashCode");
        eq(-2, "HashMap".hashCode() % 16, "% can be negative");
        eq(14, Hashing.indexFor("HashMap".hashCode(), 16), "floorMod");
        eq(14, Hashing.indexForPowerOfTwo("HashMap".hashCode(), 16), "mask");
        eq(Integer.MIN_VALUE, "polygenelubricants".hashCode(), "MIN_VALUE hash");
        eq(Integer.MIN_VALUE, Math.abs(Integer.MIN_VALUE), "abs overflow");
        // quiz with m = 7: Math.abs(h) % 7 fails for MIN_VALUE; Math.abs(h % 8) can be 7
        eq(-2, Math.abs(Integer.MIN_VALUE) % 7, "abs(MIN) % 7 is negative");
        eq(-2, Integer.MIN_VALUE % 7, "MIN % 7 is negative");
        eq(5, Math.floorMod(Integer.MIN_VALUE, 7), "floorMod(MIN, 7)");
        eq(7, Math.abs(15 % 8), "abs(h % 8) can be 7");
        eq(0, Math.abs(Integer.MIN_VALUE) % 16, "abs(MIN) % 16 is valid");
        for (int m = 1; m <= 1024; m++) {
            boolean powerOfTwo = (m & (m - 1)) == 0;
            eq(powerOfTwo, Math.abs(Integer.MIN_VALUE) % m >= 0, "abs(MIN) % m valid iff m is 2^k, m=" + m);
        }
        Random rnd = new Random(7);
        for (int i = 0; i < 10000; i++) {
            int h = rnd.nextInt();
            int m = 1 + rnd.nextInt(100);
            int idx = Hashing.indexFor(h, m);
            check(idx >= 0 && idx < m, "floorMod range");
            int p = 1 << rnd.nextInt(20);
            eq(Math.floorMod(h, p), Hashing.indexForPowerOfTwo(h, p), "mask equals floorMod for 2^k");
        }
    }

    static void runningExampleChaining() {
        ChainingHashMap<Integer, String> map = new ChainingHashMap<>(7, 0.75);
        for (int k : Trace.KEYS) {
            map.put(k, "v" + k);
        }
        eq("0:\n1:\n2: 23 -> 44\n3:\n4: 88\n5: 12\n6: 13\n", map.bucketString(), "chaining state");
        eq(5, map.size(), "size");
        eq("v23", map.get(23), "get 23");
        eq(null, map.get(30), "get absent 30 (bucket 2)");
        eq("v44", map.put(44, "new"), "put returns old value");
        eq(5, map.size(), "update keeps size");
        map.put(51, "v51");
        eq(14, map.capacity(), "resize to 14");
        eq("0:\n1:\n2: 44\n3:\n4: 88\n5:\n6:\n7:\n8:\n9: 23 -> 51\n10:\n11:\n12: 12\n13: 13\n",
                map.bucketString(), "after resize");
        eq("v51", map.remove(51), "remove 51");
        eq("0:\n1:\n2: 44\n3:\n4: 88\n5:\n6:\n7:\n8:\n9: 23\n10:\n11:\n12: 12\n13: 13\n",
                map.bucketString(), "after remove");
        eq(null, map.remove(51), "remove absent");
        eq("v23", map.remove(23), "remove head");
        check(!map.containsKey(23), "23 gone");
    }

    static void runningExampleProbing() {
        LinearProbingHashMap<Integer, String> map = new LinearProbingHashMap<>(7, 0.75);
        for (int k : Trace.KEYS) {
            map.put(k, "v" + k);
        }
        eq("[_, _, 44, 23, 88, 12, 13]", map.slotString(), "probing state");
        eq(List.of(2, 3), map.probeSequence(23), "probes for 23");
        eq(List.of(2, 3, 4, 5, 6, 0), map.probeSequence(51), "unsuccessful search for 51");
        check(!map.containsKey(51), "51 absent");
        eq(List.of(2, 3, 4, 5, 6, 0), map.probeSequence(30), "quiz: search for 30");
        eq(List.of(0), map.probeSequence(7), "home slot 0 is empty: one probe");
        eq("v44", map.remove(44), "remove 44");
        eq("[_, _, X, 23, 88, 12, 13]", map.slotString(), "tombstone");
        eq(1, map.tombstones(), "one tombstone");
        check(map.containsKey(23), "23 still found past the tombstone");
        eq("v23", map.get(23), "get 23");
        map.put(51, "v51");
        eq(14, map.capacity(), "resize to 14");
        eq(0, map.tombstones(), "rebuild clears tombstones");
        eq("[_, _, _, _, 88, _, _, _, _, 23, 51, _, 12, 13]", map.slotString(), "after resize");

        NaiveDeleteTable naive = new NaiveDeleteTable(7);
        for (int k : Trace.KEYS) {
            naive.add(k);
        }
        check(naive.contains(23), "naive finds 23 before delete");
        naive.removeWrong(44);
        check(!naive.contains(23), "naive delete loses 23: the bug the slides show");
        check(naive.contains(88) && naive.contains(12), "other keys still found");

        // A tombstone is reused by a later insert when no resize is needed.
        LinearProbingHashMap<Integer, String> small = new LinearProbingHashMap<>(16, 0.5);
        small.put(1, "a");
        small.put(17, "b");                   // 17 mod 16 = 1: probes to slot 2
        small.remove(1);
        small.put(33, "c");                   // 33 mod 16 = 1: reuses the tombstone at 1
        eq(0, small.tombstones(), "tombstone reused");
        eq("c", small.get(33), "33 stored");
        eq("b", small.get(17), "17 still found");
        eq(2, small.size(), "size after reuse");
    }

    static void contract() {
        Point p = new Point(1, 2);
        Point q = new Point(1, 2);
        check(p.equals(q) && p.hashCode() == q.hashCode(), "equal points, equal hash codes");
        HashSet<Point> good = new HashSet<>();
        good.add(p);
        check(good.contains(q), "HashSet finds an equal Point");
        ChainingHashMap<Point, String> goodMap = new ChainingHashMap<>();
        goodMap.put(p, "p");
        eq("p", goodMap.get(q), "our map finds an equal Point");

        // BrokenPoint: equal objects, but hashCode is Object's. Whenever the two hash codes
        // differ, java.util.HashSet (and our map, when the slots differ) misses the equal key.
        int misses = 0;
        for (int i = 0; i < 200; i++) {
            BrokenPoint a = new BrokenPoint(i, i);
            BrokenPoint b = new BrokenPoint(i, i);
            check(a.equals(b), "BrokenPoint equals");
            HashSet<BrokenPoint> set = new HashSet<>();
            set.add(a);
            if (a.hashCode() != b.hashCode()) {
                check(!set.contains(b), "different hash codes: HashSet misses the equal key");
                misses++;
            }
            ChainingHashMap<BrokenPoint, String> ours = new ChainingHashMap<>(64, 0.75);
            ours.put(a, "a");
            if (Math.floorMod(a.hashCode(), 64) != Math.floorMod(b.hashCode(), 64)) {
                eq(null, ours.get(b), "different slots: our map misses the equal key");
            }
        }
        check(misses > 0, "the broken contract is observable");
    }

    static void edgeCases() {
        ChainingHashMap<String, Integer> c = new ChainingHashMap<>(1, 0.75);
        eq(null, c.get("x"), "empty get");
        eq(null, c.remove("x"), "empty remove");
        eq(0, c.size(), "empty size");
        c.put("HashMap", 1);                  // negative hashCode must not break indexing
        eq(1, c.get("HashMap"), "negative hash key");
        LinearProbingHashMap<String, Integer> l = new LinearProbingHashMap<>(2, 0.5);
        l.put("HashMap", 1);
        l.put("polygenelubricants", 2);
        eq(1, l.get("HashMap"), "negative hash key");
        eq(2, l.get("polygenelubricants"), "MIN_VALUE hash key");
        c.put("n", null);
        check(c.containsKey("n") && c.get("n") == null, "null value stored");
        boolean threw = false;
        try {
            c.put(null, 1);
        } catch (NullPointerException e) {
            threw = true;
        }
        check(threw, "null key rejected by ChainingHashMap");
        threw = false;
        try {
            l.put(null, 1);
        } catch (NullPointerException e) {
            threw = true;
        }
        check(threw, "null key rejected by LinearProbingHashMap");
        HashMap<String, Integer> java = new HashMap<>();
        java.put(null, 5);
        eq(5, java.get(null), "java.util.HashMap allows one null key");
    }

    static void randomAgainstHashMap(long seed) {
        Random rnd = new Random(seed);
        ChainingHashMap<Integer, Integer> chain = new ChainingHashMap<>(2, 0.75);
        LinearProbingHashMap<Integer, Integer> probe = new LinearProbingHashMap<>(2, 0.5);
        Map<Integer, Integer> ref = new HashMap<>();
        for (int step = 0; step < 50000; step++) {
            int key = rnd.nextInt(500) - 250;  // negatives too
            int op = rnd.nextInt(4);
            if (op == 0 || op == 1) {
                int v = rnd.nextInt();
                Integer expected = ref.put(key, v);
                eq(expected, chain.put(key, v), "chain put");
                eq(expected, probe.put(key, v), "probe put");
            } else if (op == 2) {
                Integer expected = ref.remove(key);
                eq(expected, chain.remove(key), "chain remove");
                eq(expected, probe.remove(key), "probe remove");
            } else {
                eq(ref.get(key), chain.get(key), "chain get");
                eq(ref.get(key), probe.get(key), "probe get");
                eq(ref.containsKey(key), chain.containsKey(key), "chain containsKey");
                eq(ref.containsKey(key), probe.containsKey(key), "probe containsKey");
            }
            eq(ref.size(), chain.size(), "chain size");
            eq(ref.size(), probe.size(), "probe size");
            check(chain.size() <= 0.75 * chain.capacity(), "chain load factor bound");
            check(probe.size() + probe.tombstones() <= 0.5 * probe.capacity(), "probe load bound");
        }
        List<Integer> a = new ArrayList<>(chain.keys());
        List<Integer> b = new ArrayList<>(probe.keys());
        a.sort(null);
        b.sort(null);
        List<Integer> r = new ArrayList<>(ref.keySet());
        r.sort(null);
        eq(r, a, "chain key set");
        eq(r, b, "probe key set");
    }
}
