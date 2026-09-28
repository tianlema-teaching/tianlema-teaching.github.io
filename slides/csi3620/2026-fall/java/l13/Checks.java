package l13;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

/** Concrete checks for every class in l13. Prints "l13 OK" when all pass. */
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
        runningExample();
        edgeCases();
        validatorCatchesBrokenTrees();
        for (int t = 2; t <= 5; t++) {
            for (long seed = 1; seed <= 3; seed++) {
                random(t, seed);
            }
        }
        System.out.println("l13 OK");
    }

    static void runningExample() {
        String[] expected = {
            "[10]",
            "[10 20]",
            "[10 20 30]",
            "[20] / [10] [30 40]",
            "[20] / [10] [30 40 50]",
            "[20 40] / [10] [30] [50 60]",
            "[20 40] / [10] [30] [50 60 70]",
            "[20 40 60] / [10] [30] [50] [70 80]",
            "[40] / [20] [60] / [10] [30] [50] [70 80 90]",
            "[40] / [20] [60] / [10] [25 30] [50] [70 80 90]",
        };
        int[] heights = {0, 0, 0, 1, 1, 1, 1, 1, 2, 2};
        BTree<Integer> tree = new BTree<>(2);
        for (int i = 0; i < Trace.KEYS.length; i++) {
            check(tree.insert(Trace.KEYS[i]), "insert new key " + Trace.KEYS[i]);
            eq(expected[i], tree.levels(), "state after inserting " + Trace.KEYS[i]);
            eq(heights[i], tree.height(), "height after inserting " + Trace.KEYS[i]);
            BTreeValidator.validate(tree);
        }
        eq(List.of(10, 20, 25, 30, 40, 50, 60, 70, 80, 90), tree.inOrder(), "in-order");
        check(tree.contains(25) && tree.contains(90) && !tree.contains(35), "search");
        eq(3, tree.nodesRead(25), "reads for 25");
        eq(3, tree.nodesRead(35), "reads for absent 35");
        eq(1, tree.nodesRead(40), "40 is in the root");
        check(!tree.insert(30), "duplicate rejected");
        eq(10, tree.size(), "size unchanged by duplicate");
        eq(expected[9], tree.levels(), "duplicate changes nothing");
        tree.insert(100);                     // the quiz: splits the full leaf [70 80 90]
        eq("[40] / [20] [60 80] / [10] [25 30] [50] [70] [90 100]", tree.levels(), "quiz state");
        BTreeValidator.validate(tree);
    }

    static void edgeCases() {
        BTree<String> empty = new BTree<>(3);
        check(!empty.contains("a"), "empty tree has no keys");
        eq(List.of(), empty.inOrder(), "empty in-order");
        eq(0, empty.height(), "empty height");
        BTreeValidator.validate(empty);
        check(empty.insert("m"), "insert into empty");
        eq(List.of("m"), empty.inOrder(), "one key");
        BTreeValidator.validate(empty);
        boolean threw = false;
        try {
            new BTree<Integer>(1);
        } catch (IllegalArgumentException e) {
            threw = true;
        }
        check(threw, "t = 1 rejected");
        threw = false;
        try {
            empty.insert(null);
        } catch (NullPointerException e) {
            threw = true;
        }
        check(threw, "null key rejected");
    }

    static void validatorCatchesBrokenTrees() {
        BTree<Integer> tree = new BTree<>(2);
        for (int k : Trace.KEYS) {
            tree.insert(k);
        }
        BTree.Node<Integer> leaf = tree.root().children.get(0).children.get(1);   // [25 30]
        leaf.keys.set(0, 35);                 // [35 30]: keys out of order
        boolean caught = false;
        try {
            BTreeValidator.validate(tree);
        } catch (AssertionError e) {
            caught = true;
        }
        check(caught, "validator catches unsorted keys");
        leaf.keys.set(0, 25);
        BTreeValidator.validate(tree);
        leaf.keys.clear();                    // a non-root node with 0 < t-1 keys
        caught = false;
        try {
            BTreeValidator.validate(tree);
        } catch (AssertionError e) {
            caught = true;
        }
        check(caught, "validator catches an underfull node");
    }

    static void random(int t, long seed) {
        Random rnd = new Random(seed * 100 + t);
        BTree<Integer> tree = new BTree<>(t);
        TreeSet<Integer> ref = new TreeSet<>();
        for (int step = 0; step < 3000; step++) {
            int key = rnd.nextInt(5000) - 1000;
            eq(ref.add(key), tree.insert(key), "insert result t=" + t);
            if (step % 50 == 0) {
                BTreeValidator.validate(tree);
            }
            int probe = rnd.nextInt(5000) - 1000;
            eq(ref.contains(probe), tree.contains(probe), "contains t=" + t);
        }
        BTreeValidator.validate(tree);
        eq(new ArrayList<>(ref), tree.inOrder(), "in-order matches TreeSet, t=" + t);
        eq(ref.size(), tree.size(), "size t=" + t);
        int n = tree.size();
        double bound = Math.log((n + 1) / 2.0) / Math.log(t);
        check(tree.height() <= bound + 1e-9, "height bound h <= log_t((n+1)/2), t=" + t);
        for (int k : ref) {
            check(tree.nodesRead(k) <= tree.height() + 1, "reads at most h+1 nodes");
        }

        // Sorted input, the worst case for an unbalanced BST, stays shallow here.
        BTree<Integer> sorted = new BTree<>(t);
        for (int k = 0; k < 2000; k++) {
            sorted.insert(k);
        }
        BTreeValidator.validate(sorted);
        check(sorted.height() <= Math.log(2001 / 2.0) / Math.log(t) + 1e-9, "sorted input height");
    }
}
