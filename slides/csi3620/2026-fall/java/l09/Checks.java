package l09;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Concrete checks for package l09. Prints "l09 OK" when every check passes. */
public final class Checks {

    private Checks() {
    }

    static void check(boolean ok, String what) {
        if (!ok) throw new AssertionError(what);
    }

    static void eq(Object expected, Object actual, String what) {
        if (!expected.equals(actual)) throw new AssertionError(what + ": expected " + expected + " got " + actual);
    }

    static <T> List<T> pre(BinaryNode<T> r) { List<T> o = new ArrayList<>(); Traversals.preorder(r, o); return o; }
    static <T> List<T> in(BinaryNode<T> r) { List<T> o = new ArrayList<>(); Traversals.inorder(r, o); return o; }
    static <T> List<T> post(BinaryNode<T> r) { List<T> o = new ArrayList<>(); Traversals.postorder(r, o); return o; }

    public static void main(String[] args) {
        BinaryNode<Integer> t = BinaryNode.running();

        // The three depth-first orders and level order on the running tree.
        eq(List.of(50, 30, 20, 40, 35, 45, 70, 80), pre(t), "preorder");
        eq(List.of(20, 30, 35, 40, 45, 50, 70, 80), in(t), "inorder");
        eq(List.of(20, 35, 45, 40, 30, 80, 70, 50), post(t), "postorder");
        List<String> qlog = new ArrayList<>();
        eq(List.of(50, 30, 70, 20, 40, 80, 35, 45), Traversals.levelOrder(t, qlog), "level order");
        eq(List.of("50 | [30, 70]", "30 | [70, 20, 40]", "70 | [20, 40, 80]", "20 | [40, 80]",
                "40 | [80, 35, 45]", "80 | [35, 45]", "35 | [45]", "45 | []"), qlog, "queue trace");

        // Inorder of a BST is sorted.
        List<Integer> sorted = new ArrayList<>(in(t));
        Collections.sort(sorted);
        eq(sorted, in(t), "inorder sorted");

        // Iterative inorder with its stack trace.
        List<String> slog = new ArrayList<>();
        eq(in(t), Traversals.inorderIterative(t, slog), "iterative inorder");
        eq(List.of("20 | [50, 30]", "30 | [50]", "35 | [50, 40]", "40 | [50]", "45 | [50]",
                "50 | []", "70 | []", "80 | []"), slog, "stack trace");

        // Euler tour: first, second, third visits give pre, in, post.
        List<String> tour = new ArrayList<>();
        Traversals.eulerTour(t, tour);
        eq(24, tour.size(), "each node three times");
        List<Integer> p1 = new ArrayList<>(), p2 = new ArrayList<>(), p3 = new ArrayList<>();
        for (String e : tour) {
            String[] parts = e.split(" ");
            int v = Integer.parseInt(parts[1]);
            if (parts[0].equals("pre")) p1.add(v); else if (parts[0].equals("in")) p2.add(v); else p3.add(v);
        }
        eq(pre(t), p1, "tour pre"); eq(in(t), p2, "tour in"); eq(post(t), p3, "tour post");
        BinaryNode<Integer> small = new BinaryNode<>(40, BinaryNode.leaf(35), BinaryNode.leaf(45));
        List<String> smallTour = new ArrayList<>();
        Traversals.eulerTour(small, smallTour);
        eq(List.of("pre 40", "pre 35", "in 35", "post 35", "in 40", "pre 45", "in 45", "post 45", "post 40"),
                smallTour, "small tour");

        // Empty and one-node trees.
        eq(List.of(), pre(null), "empty pre");
        eq(List.of(), Traversals.levelOrder(null, null), "empty level");
        eq(List.of(), Traversals.inorderIterative(null, null), "empty iterative");
        eq(0, TreeOps.size(null), "empty size");
        eq(-1, TreeOps.height(null), "empty height");
        eq(List.of(), TreeOps.byLevels(null), "empty levels");
        eq(0, TreeOps.maxWidth(null), "empty width");
        eq(List.of(), in(null), "empty in");
        eq(List.of(), post(null), "empty post");
        List<String> emptyTour = new ArrayList<>();
        Traversals.eulerTour(null, emptyTour);
        eq(List.of(), emptyTour, "empty tour");
        check(TreeOps.copy(null) == null, "empty copy");
        check(TreeOps.sameTree(null, null), "empty same");
        eq("", ExpressionTree.prefix(null), "empty prefix");
        eq("", ExpressionTree.postfix(null), "empty postfix");
        throwsIAE(() -> ExpressionTree.evaluate(null), "evaluate rejects empty");
        throwsIAE(() -> ExpressionTree.infix(null), "infix rejects empty");
        BinaryNode<Integer> one = BinaryNode.leaf(7);
        eq(List.of(7), pre(one), "one pre"); eq(List.of(7), in(one), "one in"); eq(List.of(7), post(one), "one post");
        eq(0, TreeOps.height(one), "one height");
        List<String> oneLog = new ArrayList<>();
        eq(List.of(7), Traversals.levelOrder(one, oneLog), "one level");
        eq(List.of("7 | []"), oneLog, "one queue trace");
        oneLog.clear();
        eq(List.of(7), Traversals.inorderIterative(one, oneLog), "one iterative");
        eq(List.of("7 | []"), oneLog, "one stack trace");
        eq(1, TreeOps.size(one), "one size");
        eq(List.of(List.of(7)), TreeOps.byLevels(one), "one levels");
        eq(1, TreeOps.maxWidth(one), "one width");
        List<String> oneTour = new ArrayList<>();
        Traversals.eulerTour(one, oneTour);
        eq(List.of("pre 7", "in 7", "post 7"), oneTour, "one tour");
        BinaryNode<Integer> oneCopy = TreeOps.copy(one);
        check(oneCopy != one && TreeOps.sameTree(oneCopy, one), "one copy");
        check(TreeOps.sameTree(Rebuild.fromPreIn(List.of(7), List.of(7)), one), "one rebuild");
        BinaryNode<String> operand = BinaryNode.leaf("7");
        eq(7, ExpressionTree.evaluate(operand), "one evaluate");
        eq("7", ExpressionTree.infix(operand), "one infix");
        eq("7", ExpressionTree.prefix(operand), "one prefix");
        eq("7", ExpressionTree.postfix(operand), "one postfix");

        // Tree operations on the running tree.
        eq(8, TreeOps.size(t), "size");
        eq(3, TreeOps.height(t), "height");
        eq(List.of(List.of(50), List.of(30, 70), List.of(20, 40, 80), List.of(35, 45)), TreeOps.byLevels(t), "levels");
        eq(3, TreeOps.maxWidth(t), "max width");
        BinaryNode<Integer> c = TreeOps.copy(t);
        check(c != t && TreeOps.sameTree(c, t), "copy equal");
        c.left.value = 31;
        check(!TreeOps.sameTree(c, t) && t.left.value == 30, "copy is deep");

        // Expression tree.
        BinaryNode<String> e = BinaryNode.expression();
        eq("* + 3 4 - 5 2", ExpressionTree.prefix(e), "prefix");
        eq("((3 + 4) * (5 - 2))", ExpressionTree.infix(e), "infix");
        eq("3 4 + 5 2 - *", ExpressionTree.postfix(e), "postfix");
        eq(21, ExpressionTree.evaluate(e), "value");
        eq(List.of("3", "+", "4", "*", "5", "-", "2"), in(e), "bare inorder loses parentheses");
        BinaryNode<String> e2 = new BinaryNode<>("-", new BinaryNode<>("-", BinaryNode.leaf("8"), BinaryNode.leaf("3")), BinaryNode.leaf("2"));
        eq(3, ExpressionTree.evaluate(e2), "left-assoc minus");
        eq("((8 - 3) - 2)", ExpressionTree.infix(e2), "infix e2");
        boolean threw = false;
        try { ExpressionTree.evaluate(new BinaryNode<>("%", BinaryNode.leaf("1"), BinaryNode.leaf("2"))); }
        catch (IllegalArgumentException ex) { threw = true; }
        check(threw, "unknown operator");
        BinaryNode<String> oneChild = new BinaryNode<>("+", BinaryNode.leaf("1"), null);
        throwsIAE(() -> ExpressionTree.evaluate(oneChild), "evaluate rejects one-child operator");
        throwsIAE(() -> ExpressionTree.infix(oneChild), "infix rejects one-child operator");
        BinaryNode<String> deepOneChild = new BinaryNode<>("*", BinaryNode.leaf("2"), new BinaryNode<>("-", null, BinaryNode.leaf("5")));
        throwsIAE(() -> ExpressionTree.evaluate(deepOneChild), "evaluate rejects a deeper one-child operator");
        throwsIAE(() -> ExpressionTree.infix(deepOneChild), "infix rejects a deeper one-child operator");

        // Rebuild from preorder + inorder.
        BinaryNode<Integer> r = Rebuild.fromPreIn(pre(t), in(t));
        check(TreeOps.sameTree(r, t), "rebuild running tree");
        check(Rebuild.fromPreIn(List.<Integer>of(), List.<Integer>of()) == null, "rebuild empty");
        BinaryNode<Integer> quiz = Rebuild.fromPreIn(List.of(10, 5, 3, 7, 12), List.of(3, 5, 7, 10, 12));
        eq(List.of(3, 7, 5, 12, 10), post(quiz), "quiz: postorder of rebuilt tree");
        threw = false;
        try { Rebuild.fromPreIn(List.of(1, 1), List.of(1, 1)); } catch (IllegalArgumentException ex) { threw = true; }
        check(threw, "duplicates rejected");
        throwsIAE(() -> Rebuild.fromPreIn(List.of(1, 1), List.of(1, 2)), "repeated preorder value rejected");
        throwsIAE(() -> Rebuild.fromPreIn(List.of(1, 2, 3), List.of(3, 1, 2)), "sequences not from one tree");
        throwsIAE(() -> Rebuild.fromPreIn(List.of(1, 2), List.of(1)), "lengths differ");
        throwsIAE(() -> Rebuild.fromPreIn(List.of(1, 2), List.of(1, 3)), "different values");

        // Preorder + postorder cannot tell these two apart; inorder can.
        BinaryNode<Integer> leftOnly = new BinaryNode<>(1, BinaryNode.leaf(2), null);
        BinaryNode<Integer> rightOnly = new BinaryNode<>(1, null, BinaryNode.leaf(2));
        check(!TreeOps.sameTree(leftOnly, rightOnly), "different trees");
        eq(pre(leftOnly), pre(rightOnly), "same preorder");
        eq(post(leftOnly), post(rightOnly), "same postorder");
        check(!in(leftOnly).equals(in(rightOnly)), "different inorder");

        // Chains: height n-1, recursion depth n, stack holds everything on a left chain.
        BinaryNode<Integer> chain = null;
        for (int k = 1; k <= 1000; k++) chain = new BinaryNode<>(k, chain, null);
        eq(999, TreeOps.height(chain), "left chain height");
        List<String> clog = new ArrayList<>();
        eq(1000, Traversals.inorderIterative(chain, clog).size(), "chain iterative");
        check(clog.get(0).startsWith("1 | ") && clog.get(0).split(",").length == 999, "stack held 999 after first pop");
        eq(1, TreeOps.maxWidth(chain), "chain width 1");

        // Perfect tree of height 3: widest level holds 8 of 15 nodes.
        BinaryNode<Integer> perfect = perfect(1, 15);
        eq(15, TreeOps.size(perfect), "perfect size");
        eq(8, TreeOps.maxWidth(perfect), "perfect width (n+1)/2");

        // Random trees: iterative equals recursive; rebuild round-trips.
        Random rnd = new Random(3620);
        for (int trial = 0; trial < 300; trial++) {
            int n = rnd.nextInt(30);
            List<Integer> vals = new ArrayList<>();
            for (int i = 0; i < n; i++) vals.add(i);
            Collections.shuffle(vals, rnd);
            BinaryNode<Integer> rt = randomShape(vals, 0, n, rnd);
            eq(in(rt), Traversals.inorderIterative(rt, null), "iterative vs recursive");
            eq(n, TreeOps.size(rt), "random size");
            check(TreeOps.sameTree(rt, Rebuild.fromPreIn(pre(rt), in(rt))), "rebuild random");
            check(TreeOps.sameTree(rt, TreeOps.copy(rt)), "copy random");
            int total = 0;
            for (List<Integer> lv : TreeOps.byLevels(rt)) total += lv.size();
            eq(n, total, "levels cover all");
            eq(TreeOps.height(rt) + 1, TreeOps.byLevels(rt).size(), "levels = height + 1");
        }

        System.out.println("l09 OK");
    }

    static void throwsIAE(Runnable r, String what) {
        boolean threw = false;
        try { r.run(); } catch (IllegalArgumentException ex) { threw = true; }
        check(threw, what);
    }

    static BinaryNode<Integer> perfect(int lo, int hi) {
        if (lo > hi) return null;
        int mid = (lo + hi) >>> 1;
        return new BinaryNode<>(mid, perfect(lo, mid - 1), perfect(mid + 1, hi));
    }

    /** Arbitrary shape (not a BST) over vals[lo..hi). */
    static BinaryNode<Integer> randomShape(List<Integer> vals, int lo, int hi, Random rnd) {
        if (lo >= hi) return null;
        int leftCount = rnd.nextInt(hi - lo);
        return new BinaryNode<>(vals.get(lo), randomShape(vals, lo + 1, lo + 1 + leftCount, rnd),
                randomShape(vals, lo + 1 + leftCount, hi, rnd));
    }
}
