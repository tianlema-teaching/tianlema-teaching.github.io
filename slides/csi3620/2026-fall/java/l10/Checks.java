package l10;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

/** Concrete checks for AvlTree, PlainBst and AvlBounds. Prints "l10 OK" when all pass. */
public final class Checks {

    private Checks() { }

    private static void check(boolean ok, String what) {
        if (!ok) throw new AssertionError(what);
    }

    private static void same(Object expected, Object actual, String what) {
        if (!expected.equals(actual)) throw new AssertionError(what + ": expected " + expected + " but got " + actual);
    }

    public static void main(String[] args) {
        edgeCases();
        runningExampleInsert();
        runningExampleDelete();
        cascadingDelete();
        sortedInput();
        bounds();
        randomized();
        System.out.println("l10 OK");
    }

    static void edgeCases() {
        AvlTree<Integer> t = new AvlTree<>();
        check(t.isEmpty() && t.size() == 0, "new tree is empty");
        same(-1, t.height(), "empty height");
        same("-", t.shape(), "empty shape");
        check(!t.contains(5), "empty contains");
        check(!t.delete(5), "delete from empty");
        t.check();
        check(t.insert(5), "first insert");
        same(0, t.height(), "one node has height 0");
        check(!t.insert(5), "duplicate insert rejected");
        same(1, t.size(), "duplicate does not grow size");
        check(!t.delete(6), "delete absent key");
        check(t.delete(5) && t.isEmpty(), "delete only key");
        same(-1, t.height(), "height after emptying");
        boolean threw = false;
        try { t.insert(null); } catch (NullPointerException e) { threw = true; }
        check(threw, "null key rejected");
        t.check();
    }

    /** The lecture's running example: every state shown on the slides. */
    static void runningExampleInsert() {
        AvlTree<Integer> t = new AvlTree<>();
        List<String> mid = new ArrayList<>();
        t.setTrace(mid);
        int[] keys = {70, 50, 10, 20, 40, 30, 60};
        String[] shapes = {"70", "70(50,-)", "50(10,70)", "50(10(-,20),70)", "50(20(10,40),70)",
                "40(20(10,30),50(-,70))", "40(20(10,30),60(50,70))"};
        String[] events = {"[]", "[]", "[LL at 70]", "[]", "[RR at 10]", "[LR at 50]", "[RL at 50]"};
        String[] mids = {"[]", "[]", "[]", "[]", "[]", "[50(40(20(10,30),-),70)]", "[40(20(10,30),50(-,60(-,70)))]"};
        for (int i = 0; i < keys.length; i++) {
            check(t.insert(keys[i]), "insert " + keys[i]);
            same(shapes[i], t.shape(), "shape after inserting " + keys[i]);
            same(events[i], t.drainEvents().toString(), "rotations for " + keys[i]);
            same(mids[i], mid.toString(), "double-rotation midpoint for " + keys[i]);
            mid.clear();
            t.check();
        }
        same(2, t.height(), "seven keys, height 2");
        same(List.of(10, 20, 30, 40, 50, 60, 70), t.keys(), "inorder keys");
    }

    static void runningExampleDelete() {
        AvlTree<Integer> t = new AvlTree<>();
        for (int k : new int[] {70, 50, 10, 20, 40, 30, 60}) t.insert(k);
        t.drainEvents();
        check(t.delete(60), "delete 60");
        same("40(20(10,30),70(50,-))", t.shape(), "successor 70 replaces 60");
        same("[]", t.drainEvents().toString(), "no rotation for 60");
        check(t.delete(50), "delete 50");
        same("40(20(10,30),70)", t.shape(), "after deleting 50");
        same("[]", t.drainEvents().toString(), "no rotation for 50");
        check(t.delete(70), "delete 70");
        same("20(10,40(30,-))", t.shape(), "rotation at the root");
        same("[LL at 40]", t.drainEvents().toString(), "single rotation, left child balanced");
        t.check();
    }

    /** A 12-node tree where one deletion needs rotations at two different ancestors. */
    static void cascadingDelete() {
        AvlTree<Integer> t = new AvlTree<>();
        for (int k : new int[] {8, 3, 5, 4, 9, 6, 2, 11, 7, 12, 10, 1}) t.insert(k);
        t.drainEvents();
        same(12, t.size(), "twelve keys");
        same("8(5(3(2(1,-),4),6(-,7)),11(9(-,10),12))", t.shape(), "before the deletion");
        t.delete(11);
        same("[LR at 12, LL at 8]", t.drainEvents().toString(), "two rebalancing steps");
        same("5(3(2(1,-),4),8(6(-,7),10(9,12)))", t.shape(), "after the deletion");
        t.check();
    }

    static void sortedInput() {
        PlainBst plain = new PlainBst();
        AvlTree<Integer> avl = new AvlTree<>();
        for (int k = 1; k <= 1023; k++) {
            plain.insert(k);
            avl.insert(k);
        }
        same(1022, plain.height(), "sorted input makes a plain BST a path");
        same(9, avl.height(), "AVL on 1..1023 has height 9");
        avl.check();
        PlainBst empty = new PlainBst();
        same(-1, empty.height(), "empty plain BST");
        PlainBst running = new PlainBst();
        for (int k : new int[] {70, 50, 10, 20, 40, 30, 60}) running.insert(k);
        same(5, running.height(), "running example without balancing");
        check(empty.insert(1) && !empty.insert(1), "plain BST duplicates");
    }

    static void bounds() {
        long[] first = {1, 2, 4, 7, 12, 20, 33, 54};
        for (int h = 0; h < first.length; h++) same(first[h], AvlBounds.minNodes(h), "N(" + h + ")");
        same(0L, AvlBounds.minNodes(-1), "N(-1)");
        for (int h = 0; h <= 60; h++) {
            same(AvlBounds.fib(h + 3) - 1, AvlBounds.minNodes(h), "N(h) = F(h+3) - 1 at h=" + h);
            long n = AvlBounds.minNodes(h);
            check(h <= 1.4405 * Math.log(n + 1) / Math.log(2) - 1, "height bound at h=" + h);
        }
        same(2, AvlBounds.maxHeight(6), "6 nodes: height at most 2");
        same(3, AvlBounds.maxHeight(7), "7 nodes: height at most 3, since N(3) = 7");
    }

    static void randomized() {
        Random rnd = new Random(20260927L);
        for (int trial = 0; trial < 300; trial++) {
            AvlTree<Integer> t = new AvlTree<>();
            TreeSet<Integer> ref = new TreeSet<>();
            int range = 1 + rnd.nextInt(200);
            for (int op = 0; op < 400; op++) {
                int k = rnd.nextInt(range);
                if (rnd.nextInt(3) > 0) {
                    same(ref.add(k), t.insert(k), "insert result");
                    check(t.drainEvents().size() <= 1, "at most one rebalance per insertion");
                } else {
                    same(ref.remove(k), t.delete(k), "delete result");
                    t.drainEvents();
                }
                t.check();
                same(ref.size(), t.size(), "size");
                check(t.height() <= AvlBounds.maxHeight(t.size()), "height within the AVL bound");
            }
            same(new ArrayList<>(ref), t.keys(), "same keys as TreeSet");
            for (int k = 0; k < range; k++) same(ref.contains(k), t.contains(k), "contains " + k);
        }
    }
}
