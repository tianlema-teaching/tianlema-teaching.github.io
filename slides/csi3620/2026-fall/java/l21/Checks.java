package l21;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/** Concrete checks for every class in package l21. Prints "l21 OK" when all pass. */
public final class Checks {
    private Checks() { }

    static void check(boolean ok, String what) {
        if (!ok) throw new AssertionError(what);
    }

    static List<Integer> range(int n) {
        List<Integer> xs = new ArrayList<>();
        for (int i = 1; i <= n; i++) xs.add(i);
        return xs;
    }

    static void subsetsAndPermutations() {
        List<List<String>> s = Subsets.subsets(List.of("a", "b", "c"));
        check(s.equals(List.of(List.of("a", "b", "c"), List.of("a", "b"), List.of("a", "c"),
                List.of("a"), List.of("b", "c"), List.of("b"), List.of("c"), List.of())),
                "subsets of abc in order");
        List<List<Integer>> p = Subsets.permutations(range(3));
        check(p.equals(List.of(List.of(1, 2, 3), List.of(1, 3, 2), List.of(2, 1, 3),
                List.of(2, 3, 1), List.of(3, 1, 2), List.of(3, 2, 1))), "permutations of 123");
        long fact = 1;
        for (int n = 0; n <= 7; n++) {
            if (n > 0) fact *= n;
            List<List<Integer>> subs = Subsets.subsets(range(n));
            check(subs.size() == 1 << n, "2^n subsets for n = " + n);
            check(new HashSet<>(subs).size() == subs.size(), "subsets distinct");
            List<List<Integer>> perms = Subsets.permutations(range(n));
            check(perms.size() == fact, "n! permutations for n = " + n);
            check(new HashSet<>(perms).size() == perms.size(), "permutations distinct");
        }
        check(Subsets.subsets(List.<Integer>of()).equals(List.of(List.of())), "empty set has one subset");
    }

    static void queens() {
        long[] expected = {1, 0, 0, 2, 10, 4, 40, 92, 352, 724};   // n = 1..10
        for (int n = 1; n <= 10; n++) {
            List<int[]> sols = NQueens.solve(n);
            check(sols.size() == expected[n - 1], "solutions for n = " + n);
            for (int[] s : sols) check(NQueens.valid(s), "each solution is valid");
        }
        check(Arrays.equals(NQueens.solve(4).get(0), new int[] {1, 3, 0, 2}), "first 4-queens");
        check(Arrays.equals(NQueens.solve(4).get(1), new int[] {2, 0, 3, 1}), "second 4-queens");
        check(NQueens.nodesWithPruning(4) == 17, "17 nodes for n = 4");
        long[] prunedNodes = {17, 54, 153, 552, 2057};                 // n = 4..8
        for (int n = 4; n <= 8; n++) {
            check(NQueens.nodesWithPruning(n) == prunedNodes[n - 4], "pruned nodes n = " + n);
        }
        check(NQueens.withoutPruning(8)[0] == 19_173_961L, "unpruned nodes n = 8");
        for (int n = 1; n <= 7; n++) {
            long[] blind = NQueens.withoutPruning(n);
            long all = 0, power = 1;
            for (int k = 0; k <= n; k++) { all += power; power *= n; }
            check(blind[0] == all, "unpruned nodes are 1 + n + ... + n^n");
            check(blind[1] == expected[n - 1], "unpruned finds the same solutions");
            check(n == 1 || NQueens.nodesWithPruning(n) < blind[0], "pruning visits fewer nodes");
        }
        long[] permNodes = {65, 326, 1_957, 13_700, 109_601};            // n = 4..8
        for (int n = 0; n <= 8; n++) {
            long[] perm = NQueens.columnsOnly(n);
            long all = 0, term = 1;                                     // sum of n!/(n-k)!
            for (int k = 0; k <= n; k++) { all += term; term *= n - k; }
            check(perm[0] == all, "permutation-tree nodes n = " + n);
            if (n >= 4) check(perm[0] == permNodes[n - 4], "permutation nodes table n = " + n);
            if (n >= 1) check(perm[1] == expected[n - 1], "permutation tree finds the same solutions");
        }
        check(NQueens.solve(0).size() == 1 && NQueens.solve(0).get(0).length == 0, "0 queens: one empty solution");
        boolean threw = false;
        try { NQueens.solve(-1); } catch (IllegalArgumentException e) { threw = true; }
        check(threw, "negative n rejected");
        List<String> trace = NQueens.trace(4);
        check(trace.subList(0, 21).equals(List.of(
                "place row 0 col 0", "reject row 1 col 0", "reject row 1 col 1", "place row 1 col 2",
                "reject row 2 col 0", "reject row 2 col 1", "reject row 2 col 2", "reject row 2 col 3",
                "remove row 1 col 2", "place row 1 col 3", "reject row 2 col 0", "place row 2 col 1",
                "reject row 3 col 0", "reject row 3 col 1", "reject row 3 col 2", "reject row 3 col 3",
                "remove row 2 col 1", "reject row 2 col 2", "reject row 2 col 3", "remove row 1 col 3",
                "remove row 0 col 0")), "4-queens trace with the corner queen");
        check(trace.subList(21, 31).equals(List.of(
                "place row 0 col 1", "reject row 1 col 0", "reject row 1 col 1", "reject row 1 col 2",
                "place row 1 col 3", "place row 2 col 0", "reject row 3 col 0", "reject row 3 col 1",
                "place row 3 col 2", "solution [1, 3, 0, 2]")), "4-queens trace to the first solution");
        check(trace.indexOf("solution [2, 0, 3, 1]") == 47, "second solution in the trace");
        check(trace.get(trace.size() - 1).equals("remove row 0 col 3"), "trace ends by removing (0, 3)");
        check(trace.stream().filter(e -> e.startsWith("place")).count() == 16, "16 placements + root = 17 nodes");
    }

    static void subsetSum() {
        int[] a = {2, 3, 5, 6, 8};
        List<List<Integer>> pruned = SubsetSum.subsets(a, 10, true);
        long prunedNodes = SubsetSum.lastNodes();
        check(pruned.equals(List.of(List.of(2, 3, 5), List.of(2, 8))), "subsets summing to 10");
        List<List<Integer>> blind = SubsetSum.subsets(a, 10, false);
        long blindNodes = SubsetSum.lastNodes();
        check(blind.equals(pruned), "same answers without pruning");
        check(prunedNodes == 13 && blindNodes == 29, "node counts " + prunedNodes + " " + blindNodes);
        check(SubsetSum.combinations(new int[] {1, 3, 4}, 6).equals(List.of(
                List.of(1, 1, 1, 1, 1, 1), List.of(1, 1, 1, 3), List.of(1, 1, 4), List.of(3, 3))),
                "combination sum matches L20 count of 4");
        check(SubsetSum.lastNodes() == 15, "combination sum visits 15 nodes");
        check(SubsetSum.subsets(a, 0, true).equals(List.of(List.of())), "target 0");
        check(SubsetSum.subsets(a, 1, true).isEmpty(), "impossible target");
        Random rnd = new Random(21);
        for (int t = 0; t < 300; t++) {
            int n = rnd.nextInt(9);
            Set<Integer> distinct = new HashSet<>();
            while (distinct.size() < n) distinct.add(1 + rnd.nextInt(15));
            int[] xs = distinct.stream().mapToInt(Integer::intValue).sorted().toArray();
            int target = rnd.nextInt(30);
            int brute = 0;
            for (int mask = 0; mask < (1 << n); mask++) {
                int sum = 0;
                for (int i = 0; i < n; i++) if ((mask & (1 << i)) != 0) sum += xs[i];
                if (sum == target) brute++;
            }
            check(SubsetSum.subsets(xs, target, true).size() == brute, "pruned vs brute");
            check(SubsetSum.subsets(xs, target, false).size() == brute, "unpruned vs brute");
        }
    }

    public static void main(String[] args) {
        subsetsAndPermutations();
        queens();
        subsetSum();
        System.out.println("l21 OK");
    }
}
