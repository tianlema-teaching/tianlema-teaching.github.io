package l08;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;
import java.util.TreeSet;

/** Concrete checks for package l08. Prints "l08 OK" when every check passes. */
public final class Checks {

    private Checks() {
    }

    static void check(boolean ok, String what) {
        if (!ok) throw new AssertionError(what);
    }

    static void eq(Object expected, Object actual, String what) {
        if (!expected.equals(actual)) throw new AssertionError(what + ": expected " + expected + " got " + actual);
    }

    static BST<Integer> running() {
        BST<Integer> t = new BST<>();
        for (int k : LibraryDemo.RUNNING) check(t.insert(k), "insert " + k);
        return t;
    }

    public static void main(String[] args) {
        // Empty tree edge cases.
        BST<Integer> empty = new BST<>();
        check(empty.isEmpty() && empty.size() == 0, "empty");
        eq(-1, empty.height(), "empty height");
        check(!empty.contains(5), "empty contains");
        check(empty.successor(5) == null, "empty successor");
        check(!empty.delete(5), "empty delete");
        eq("-", empty.shape(), "empty shape");
        boolean threw = false;
        try { empty.min(); } catch (IllegalStateException e) { threw = true; }
        check(threw, "min of empty throws");
        rejectsNullKeys(empty, "empty");

        // One node.
        BST<Integer> one = new BST<>();
        one.insert(7);
        eq(0, one.height(), "single height");
        eq(0, one.depth(7), "single depth");
        eq(7, one.min(), "single min");
        eq(7, one.max(), "single max");
        check(one.delete(7) && one.isEmpty(), "delete only node");

        // The running tree, as drawn on the slides.
        // Building it: the keys compared before each insert (the table on the slides).
        List<List<Integer>> before = List.of(List.of(), List.of(50), List.of(50), List.of(50, 30),
                List.of(50, 30), List.of(50, 70), List.of(50, 30, 40), List.of(50, 30, 40));
        BST<Integer> built = new BST<>();
        for (int i = 0; i < LibraryDemo.RUNNING.size(); i++) {
            int k = LibraryDemo.RUNNING.get(i);
            eq(before.get(i), built.searchPath(k), "keys compared before inserting " + k);
            check(built.insert(k), "insert " + k);
        }
        BST<Integer> t = running();
        eq(t.shape(), built.shape(), "built tree is the running tree");
        rejectsNullKeys(t, "running");
        eq(8, t.size(), "null keys change nothing");
        eq("50(30(20,40(35,45)),70(-,80))", t.shape(), "running shape");
        eq(8, t.size(), "running size");
        eq(3, t.height(), "running height");
        eq(0, t.depth(50), "depth 50");
        eq(2, t.depth(40), "depth 40");
        eq(3, t.depth(45), "depth 45");
        eq(-1, t.depth(42), "depth absent");
        eq(20, t.min(), "min");
        eq(80, t.max(), "max");
        check(t.isValid(), "valid");
        eq(List.of(20, 30, 35, 40, 45, 50, 70, 80), t.keys(), "sorted keys");

        // Search paths.
        eq(List.of(50, 30, 40, 45), t.searchPath(45), "search 45");
        eq(List.of(50, 30, 40, 45), t.searchPath(42), "search 42 misses after 45");
        eq(List.of(50, 70, 80), t.searchPath(80), "search 80");
        check(t.contains(35) && !t.contains(42) && !t.contains(90), "contains");

        // Duplicates are ignored.
        check(!t.insert(40), "duplicate insert returns false");
        eq(8, t.size(), "size after duplicate");
        eq("50(30(20,40(35,45)),70(-,80))", t.shape(), "shape after duplicate");

        // Successor, both cases.
        eq(35, t.successor(30), "succ 30: min of right subtree");
        eq(50, t.successor(45), "succ 45: lowest ancestor reached by going left");
        eq(70, t.successor(50), "succ 50");
        eq(40, t.successor(35), "succ 35");
        check(t.successor(80) == null, "succ of max");
        eq(45, t.successor(42), "succ of absent key");
        eq(45, t.successor(40), "succ 40 (quiz)");
        eq(30, t.successor(20), "succ 20");

        // Delete, three cases, each from a fresh copy of the running tree.
        BST<Integer> a = running();
        check(a.delete(20), "delete leaf 20");
        eq("50(30(-,40(35,45)),70(-,80))", a.shape(), "after delete 20");
        check(a.isValid() && a.size() == 7, "valid after leaf delete");

        BST<Integer> b = running();
        check(b.delete(70), "delete one-child 70");
        eq("50(30(20,40(35,45)),80)", b.shape(), "after delete 70");
        check(b.isValid() && b.size() == 7, "valid after one-child delete");

        BST<Integer> c = running();
        check(c.delete(30), "delete two-children 30");
        eq("50(35(20,40(-,45)),70(-,80))", c.shape(), "after delete 30");
        check(c.isValid() && c.size() == 7, "valid after two-child delete");

        BST<Integer> d = running();
        check(d.delete(50), "delete root 50");
        eq("70(30(20,40(35,45)),80)", d.shape(), "after delete root");
        check(!d.delete(42), "delete absent");
        eq(7, d.size(), "size after absent delete");

        BST<Integer> e = running();
        check(e.delete(40), "delete 40 with two children");
        eq("50(30(20,45(35,-)),70(-,80))", e.shape(), "successor is right child");

        // Sorted insertion builds a chain.
        BST<Integer> chain = new BST<>();
        for (int k : List.of(20, 30, 35, 40, 45, 50, 70, 80)) chain.insert(k);
        eq(7, chain.height(), "chain height n-1");
        eq("20(-,30(-,35(-,40(-,45(-,50(-,70(-,80)))))))", chain.shape(), "chain shape");
        eq(8, chain.searchPath(80).size(), "chain search visits all");

        BST<Integer> ins = running();
        check(ins.insert(42), "insert 42");
        eq("50(30(20,40(35,45(42,-))),70(-,80))", ins.shape(), "42 lands left of 45");
        eq(4, ins.height(), "height after 42");

        BST<Integer> big = new BST<>();
        for (int k = 1; k <= 1000; k++) big.insert(k);
        eq(999, big.height(), "sorted 1..1000 gives height 999");

        // Height bounds: floor(log2 n) <= h <= n-1, and perfect-tree counts.
        for (int n = 1; n <= 63; n++) {
            BST<Integer> bal = new BST<>();
            insertMiddleFirst(bal, 0, n - 1);
            int floorLog = 31 - Integer.numberOfLeadingZeros(n);
            eq(floorLog, bal.height(), "balanced height for n=" + n);
            if (Integer.bitCount(n + 1) == 1) eq(n, (1 << (bal.height() + 1)) - 1, "perfect count n=" + n);
        }
        Random rnd = new Random(3620);
        for (int trial = 0; trial < 200; trial++) {
            BST<Integer> r = new BST<>();
            TreeSet<Integer> ref = new TreeSet<>();
            for (int i = 0; i < 40; i++) {
                int k = rnd.nextInt(60);
                eq(ref.add(k), r.insert(k), "random insert");
            }
            int n = r.size();
            check(r.height() >= 31 - Integer.numberOfLeadingZeros(n) && r.height() <= n - 1, "height bounds");
            for (int i = 0; i < 30; i++) {
                int k = rnd.nextInt(60);
                eq(ref.higher(k) == null ? "null" : ref.higher(k), r.successor(k) == null ? "null" : r.successor(k), "succ vs TreeSet");
                eq(ref.remove(k), r.delete(k), "random delete");
                check(r.isValid(), "valid after random delete");
            }
            eq(new ArrayList<>(ref), r.keys(), "keys vs TreeSet");
        }

        // Library.
        TreeMap<Integer, String> m = LibraryDemo.roster();
        eq(List.of(20, 80, 40, 45, 50), LibraryDemo.queries(m), "TreeMap queries");
        eq("record 4", m.get(40), "get 40");
        eq(List.of(20, 30, 35, 40, 45, 50, 70, 80), new ArrayList<>(m.keySet()), "TreeMap key order");
        TreeSet<Integer> s = LibraryDemo.keySet();
        eq(8, s.size(), "TreeSet ignores duplicate");
        check(!s.add(40), "TreeSet add duplicate false");
        String old = m.put(40, "replaced");
        eq("record 4", old, "put on existing key returns old value");
        eq(8, m.size(), "put existing keeps size");
        threw = false;
        try { m.put(null, "x"); } catch (NullPointerException ex) { threw = true; }
        check(threw, "TreeMap natural ordering rejects null key");

        System.out.println("l08 OK");
    }

    /** Every public method that takes a key must throw NullPointerException for null. */
    static void rejectsNullKeys(BST<Integer> t, String name) {
        List<Runnable> calls = List.of(() -> t.contains(null), () -> t.searchPath(null), () -> t.insert(null),
                () -> t.successor(null), () -> t.delete(null), () -> t.depth(null));
        for (int i = 0; i < calls.size(); i++) {
            boolean threw = false;
            try { calls.get(i).run(); } catch (NullPointerException e) { threw = "null key".equals(e.getMessage()); }
            check(threw, name + ": null key rejected by call " + i);
        }
    }

    static void insertMiddleFirst(BST<Integer> t, int lo, int hi) {
        if (lo > hi) return;
        int mid = (lo + hi) >>> 1;
        t.insert(mid);
        insertMiddleFirst(t, lo, mid - 1);
        insertMiddleFirst(t, mid + 1, hi);
    }
}
