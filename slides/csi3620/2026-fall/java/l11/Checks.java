package l11;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

/** Concrete checks for RedBlackTree. Prints "l11 OK" when all pass. */
public final class Checks {

    private Checks() { }

    private static void check(boolean ok, String what) {
        if (!ok) throw new AssertionError(what);
    }

    private static void same(Object expected, Object actual, String what) {
        if (!expected.equals(actual)) throw new AssertionError(what + ": expected " + expected + " but got " + actual);
    }

    private static double log2(double x) { return Math.log(x) / Math.log(2); }

    public static void main(String[] args) {
        edgeCases();
        runningExample();
        validatorCatchesBrokenTrees();
        sortedInput();
        randomized();
        System.out.println("l11 OK");
    }

    static void edgeCases() {
        RedBlackTree<Integer> t = new RedBlackTree<>();
        check(t.isEmpty(), "new tree is empty");
        same(0, t.check(), "empty tree: black-height 0");
        same(-1, t.height(), "empty height");
        same("-", t.shape(), "empty shape");
        same("-", t.asTwoThreeFour(), "empty 2-3-4 reading");
        check(!t.contains(1), "empty contains");
        check(t.insert(1), "first insert");
        same("B1", t.shape(), "a lone root is black");
        same(1, t.check(), "one black node: black-height 1");
        check(!t.insert(1), "duplicate rejected");
        same(1, t.size(), "duplicate does not grow size");
        boolean threw = false;
        try { t.insert(null); } catch (NullPointerException e) { threw = true; }
        check(threw, "null key rejected");
    }

    /** The lecture's running example: every state shown on the slides. */
    static void runningExample() {
        RedBlackTree<Integer> t = new RedBlackTree<>();
        List<String> steps = new ArrayList<>();
        t.setTrace(steps);
        int[] keys = {10, 60, 20, 30, 50, 40};
        String[] shapes = {"B10", "B10(-,R60)", "B20(R10,R60)", "B20(B10,B60(R30,-))",
                "B20(B10,B50(R30,R60))", "B20(B10,R50(B30(-,R40),B60))"};
        String[] events = {"[]", "[]", "[mirror case 2 at 20, mirror case 3 at 60]", "[mirror case 1 at 30]",
                "[case 2 at 50, case 3 at 30]", "[case 1 at 40]"};
        String[] trace = {"[]", "[]", "[B10(-,R20(-,R60)), B20(R10,R60)]", "[R20(B10,B60(R30,-))]",
                "[B20(B10,B60(R50(R30,-),-)), B20(B10,B50(R30,R60))]", "[B20(B10,R50(B30(-,R40),B60))]"};
        int[] blackHeights = {1, 1, 1, 2, 2, 2};
        for (int i = 0; i < keys.length; i++) {
            check(t.insert(keys[i]), "insert " + keys[i]);
            same(shapes[i], t.shape(), "shape after inserting " + keys[i]);
            same(events[i], t.drainEvents().toString(), "fix-up cases for " + keys[i]);
            same(trace[i], steps.toString(), "fix-up steps for " + keys[i]);
            steps.clear();
            same(blackHeights[i], t.check(), "black-height after " + keys[i]);
        }
        same(3, t.height(), "six keys, height 3");
        same(List.of(10, 20, 30, 40, 50, 60), t.keys(), "inorder keys");
        same("[20 50]([10],[30 40],[60])", t.asTwoThreeFour(), "2-3-4 reading");
    }

    /** The validator must reject each broken property, not just accept good trees. */
    static void validatorCatchesBrokenTrees() {
        RedBlackTree<Integer> t = new RedBlackTree<>();
        for (int k : new int[] {10, 60, 20, 30, 50, 40}) t.insert(k);
        RedBlackTree<Integer>.Node root = t.rootNode();      // B20(B10,R50(B30(-,R40),B60))
        root.color = true;                                   // property 2: red root
        check(fails(t), "red root detected");
        root.color = false;
        RedBlackTree<Integer>.Node n30 = root.right.left;
        n30.color = true;                                    // property 4: red 30 under red 50
        check(fails(t), "red-red detected");
        n30.color = false;
        n30.right.color = false;                             // property 5: black 40 under 30 only
        check(fails(t), "unequal black counts detected");
        n30.right.color = true;
        same(2, t.check(), "restored tree passes again");
    }

    private static boolean fails(RedBlackTree<Integer> t) {
        try { t.check(); return false; } catch (AssertionError e) { return true; }
    }

    static void sortedInput() {
        RedBlackTree<Integer> t = new RedBlackTree<>();
        for (int k = 1; k <= 1023; k++) t.insert(k);
        t.check();
        check(t.height() <= 2 * log2(t.size() + 1), "sorted input stays within 2 log2(n+1)");
        same(1023, t.size(), "size");
    }

    static void randomized() {
        Random rnd = new Random(20260927L);
        for (int trial = 0; trial < 300; trial++) {
            RedBlackTree<Integer> t = new RedBlackTree<>();
            TreeSet<Integer> ref = new TreeSet<>();
            int range = 1 + rnd.nextInt(500);
            for (int op = 0; op < 300; op++) {
                int k = rnd.nextInt(range);
                same(ref.add(k), t.insert(k), "insert result");
                long rotations = t.drainEvents().stream().filter(e -> !e.contains("case 1")).count();
                check(rotations <= 2, "at most two rotations per insertion");
                int bh = t.check();
                int h = t.height(), n = t.size();
                check(2 * bh >= h + 1, "black-height at least half the longest path");
                check(n >= (1L << bh) - 1, "at least 2^bh - 1 nodes");
                check(h <= 2 * log2(n + 1), "height at most 2 log2(n+1)");
            }
            same(new ArrayList<>(ref), t.keys(), "same keys as TreeSet");
            for (int k = 0; k < range; k++) same(ref.contains(k), t.contains(k), "contains " + k);
        }
    }
}
