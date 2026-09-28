package l19;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/** Concrete checks for every class in l19; prints "l19 OK". */
public final class Checks {
    private Checks() { }

    static void check(boolean ok, String what) {
        if (!ok) throw new AssertionError(what);
    }

    static void eq(Object expected, Object actual, String what) {
        if (!expected.equals(actual)) {
            throw new AssertionError(what + ": expected " + expected + " but got " + actual);
        }
    }

    public static void main(String[] args) {
        intervals();
        knapsack();
        huffman();
        coins();
        System.out.println("l19 OK");
    }

    static Interval iv(String n, int s, int f) { return new Interval(n, s, f); }

    static void intervals() {
        check(iv("p", 1, 3).compatibleWith(iv("q", 3, 5)), "touching ends are compatible");
        check(!iv("p", 1, 4).compatibleWith(iv("q", 3, 5)), "overlap");
        boolean threw = false;
        try { iv("z", 2, 2); } catch (IllegalArgumentException e) { threw = true; }
        check(threw, "empty interval rejected");

        List<Interval> run = RunningExample.intervals();
        List<String> log = new ArrayList<>();
        List<Interval> eft = IntervalScheduling.earliestFinish(run, log);
        eq(List.of("take a[1,4)", "skip b[3,5)", "skip c[0,6)", "take d[5,7)", "skip e[3,8)",
                "skip f[5,9)", "skip g[6,10)", "take h[8,11)"), log, "EFT trace");
        eq(3, eft.size(), "EFT picks three");
        eq(3, bestCount(run), "three is the maximum");
        eq(List.of(iv("c", 0, 6), iv("g", 6, 10)),
                IntervalScheduling.greedyBy(run, IntervalScheduling.EARLIEST_START),
                "earliest start fails on the running example");
        eq(3, IntervalScheduling.greedyBy(run, IntervalScheduling.SHORTEST_FIRST).size(),
                "shortest first happens to work on the running example");

        List<Interval> shortBad = List.of(iv("p", 0, 5), iv("q", 4, 6), iv("r", 5, 10));
        eq(List.of(iv("q", 4, 6)), IntervalScheduling.greedyBy(shortBad, IntervalScheduling.SHORTEST_FIRST),
                "shortest first takes only q");
        eq(2, bestCount(shortBad), "p and r fit together");
        eq(2, IntervalScheduling.earliestFinish(shortBad).size(), "EFT finds 2");

        List<Interval> startBad = List.of(iv("p", 0, 10), iv("q", 1, 2), iv("r", 3, 4));
        eq(1, IntervalScheduling.greedyBy(startBad, IntervalScheduling.EARLIEST_START).size(),
                "earliest start takes only p");
        eq(2, bestCount(startBad), "q and r fit together");

        check(IntervalScheduling.earliestFinish(List.of()).isEmpty(), "no intervals");
        Random rnd = new Random(19);
        for (int t = 0; t < 3000; t++) {
            List<Interval> xs = new ArrayList<>();
            int n = rnd.nextInt(11);
            for (int i = 0; i < n; i++) {
                int s = rnd.nextInt(12);
                xs.add(iv("i" + i, s, s + 1 + rnd.nextInt(6)));
            }
            List<Interval> got = IntervalScheduling.earliestFinish(xs);
            check(pairwiseCompatible(got), "EFT output compatible");
            eq(bestCount(xs), got.size(), "EFT is optimal");
        }
    }

    static boolean pairwiseCompatible(List<Interval> xs) {
        for (int i = 0; i < xs.size(); i++)
            for (int j = i + 1; j < xs.size(); j++)
                if (!xs.get(i).compatibleWith(xs.get(j))) return false;
        return true;
    }

    /** Brute force: the largest pairwise compatible subset. */
    static int bestCount(List<Interval> xs) {
        int best = 0;
        for (int mask = 0; mask < (1 << xs.size()); mask++) {
            List<Interval> pick = new ArrayList<>();
            for (int i = 0; i < xs.size(); i++) if ((mask & (1 << i)) != 0) pick.add(xs.get(i));
            if (pairwiseCompatible(pick)) best = Math.max(best, pick.size());
        }
        return best;
    }

    static void knapsack() {
        List<Item> items = RunningExample.items();
        int cap = RunningExample.CAPACITY;
        eq(5.0, items.get(0).ratio(), "X ratio");
        eq(46.0, Knapsack.fractional(items, cap), "fractional: X plus 4/5 of Y");
        eq(46.0, unitPieces(items, cap), "fractional matches unit-piece optimum");
        eq(30, Knapsack.greedyZeroOne(items, cap), "0/1 greedy takes X only");
        eq(35, bestZeroOne(items, cap), "0/1 optimum is Y + Z");
        eq(0.0, Knapsack.fractional(items, 0), "zero capacity");
        eq(65.0, Knapsack.fractional(items, 100), "everything fits");
        boolean threw = false;
        try { new Item("w", 1, 0); } catch (IllegalArgumentException e) { threw = true; }
        check(threw, "zero weight rejected");
        threw = false;
        try { Knapsack.fractional(items, -1); } catch (IllegalArgumentException e) { threw = true; }
        check(threw, "negative capacity rejected");
        Random rnd = new Random(7);
        int greedyMisses = 0;
        for (int t = 0; t < 3000; t++) {
            List<Item> xs = new ArrayList<>();
            int n = rnd.nextInt(8);
            for (int i = 0; i < n; i++) xs.add(new Item("i" + i, rnd.nextInt(30), 1 + rnd.nextInt(10)));
            int c = rnd.nextInt(25);
            double frac = Knapsack.fractional(xs, c);
            check(Math.abs(frac - unitPieces(xs, c)) < 1e-9, "fractional optimal");
            int best = bestZeroOne(xs, c);
            check(frac + 1e-9 >= best, "fractional bounds 0/1");
            int g = Knapsack.greedyZeroOne(xs, c);
            check(g <= best, "greedy 0/1 is feasible");
            if (g < best) greedyMisses++;
        }
        check(greedyMisses > 0, "random search also finds 0/1 greedy failures");
    }

    /** Split every item into unit-weight pieces and take the most valuable pieces. */
    static double unitPieces(List<Item> items, int cap) {
        List<Double> pieces = new ArrayList<>();
        for (Item it : items) for (int k = 0; k < it.weight(); k++) pieces.add(it.ratio());
        pieces.sort(Collections.reverseOrder());
        double sum = 0;
        for (int i = 0; i < Math.min(cap, pieces.size()); i++) sum += pieces.get(i);
        return sum;
    }

    /** Brute force over every subset of whole items. */
    static int bestZeroOne(List<Item> items, int cap) {
        int best = 0;
        for (int mask = 0; mask < (1 << items.size()); mask++) {
            int w = 0, v = 0;
            for (int i = 0; i < items.size(); i++) {
                if ((mask & (1 << i)) != 0) { w += items.get(i).weight(); v += items.get(i).value(); }
            }
            if (w <= cap) best = Math.max(best, v);
        }
        return best;
    }

    static void huffman() {
        Map<Character, Long> f = RunningExample.frequencies();
        List<String> log = new ArrayList<>();
        Map<Character, String> code = Huffman.codes(f, log);
        eq(List.of("merge F:1 + E:3 = 4", "merge (FE):4 + D:5 = 9", "merge C:7 + (FED):9 = 16",
                "merge B:10 + A:14 = 24", "merge (CFED):16 + (BA):24 = 40"), log, "Huffman trace");
        eq(Map.of('A', "11", 'B', "10", 'C', "00", 'D', "011", 'E', "0101", 'F', "0100"), code, "codes");
        eq(93L, Huffman.totalBits(f, code), "Huffman bits");
        long mergeSum = 0;
        for (String line : log) mergeSum += Long.parseLong(line.substring(line.lastIndexOf(' ') + 1));
        eq(93L, mergeSum, "total bits equal the sum of the merged weights");
        eq(120L, 40L * 3, "fixed-length bits: 40 symbols times 3 bits");
        eq(93L, bestLengths(f), "brute force optimum over prefix codes");
        check(prefixFree(code), "prefix-free");
        eq(Map.of('Q', "0"), Huffman.codes(Map.of('Q', 9L)), "one symbol gets one bit");
        check(Huffman.codes(Map.of()).isEmpty(), "no symbols");
        Map<Character, Long> quiz = Map.of('P', 2L, 'Q', 3L, 'R', 4L, 'S', 9L);
        List<String> quizLog = new ArrayList<>();
        Map<Character, String> quizCode = Huffman.codes(quiz, quizLog);
        eq(List.of("merge P:2 + Q:3 = 5", "merge R:4 + (PQ):5 = 9", "merge S:9 + (RPQ):9 = 18"),
                quizLog, "quiz merges");
        eq(32L, Huffman.totalBits(quiz, quizCode), "quiz total bits");
        Map<Character, Long> flat = new TreeMap<>();
        for (char ch = 'A'; ch <= 'F'; ch++) flat.put(ch, 1L);
        eq(16L, Huffman.totalBits(flat, Huffman.codes(flat)), "six equal counts: 16 bits, not 18");
        Random rnd = new Random(3);
        for (int t = 0; t < 400; t++) {
            Map<Character, Long> g = new TreeMap<>();
            int n = 2 + rnd.nextInt(5);
            for (int i = 0; i < n; i++) g.put((char) ('a' + i), 1L + rnd.nextInt(20));
            Map<Character, String> c = Huffman.codes(g);
            check(prefixFree(c), "random prefix-free");
            eq(bestLengths(g), Huffman.totalBits(g, c), "Huffman optimal");
        }
    }

    static boolean prefixFree(Map<Character, String> code) {
        for (String a : code.values())
            for (String b : code.values())
                if (a != b && b.startsWith(a)) return false;
        return true;
    }

    /**
     * Brute force: minimum of sum f*len over code lengths 1..n-1 with sum 2^-len <= 1
     * (Kraft: exactly the lengths some prefix-free code can have).
     */
    static long bestLengths(Map<Character, Long> f) {
        long[] w = f.values().stream().mapToLong(Long::longValue).toArray();
        int n = w.length;
        int[] len = new int[n];
        Arrays.fill(len, 1);
        long best = Long.MAX_VALUE;
        while (true) {
            long kraft = 0;                              // in units of 2^-(n-1)
            long cost = 0;
            for (int i = 0; i < n; i++) { kraft += 1L << (n - 1 - len[i]); cost += w[i] * len[i]; }
            if (kraft <= (1L << (n - 1))) best = Math.min(best, cost);
            int i = 0;
            while (i < n && len[i] == n - 1) { len[i] = 1; i++; }
            if (i == n) break;
            len[i]++;
        }
        return best;
    }

    static void coins() {
        int[] us = {1, 5, 10, 25};
        eq(List.of(25, 25, 10, 5, 1, 1), CoinChange.greedy(us, 67), "67 cents");
        for (int a = 0; a <= 500; a++) eq(fewest(us, a), CoinChange.greedy(us, a).size(), "US coins " + a);
        int[] odd = {1, 3, 4};
        eq(List.of(4, 1, 1), CoinChange.greedy(odd, 6), "greedy on {1,3,4}");
        eq(2, fewest(odd, 6), "3 + 3 is optimal");
        check(CoinChange.greedy(odd, 0).isEmpty(), "zero amount");
        boolean threw = false;
        try { CoinChange.greedy(new int[] {3, 4}, 6); } catch (IllegalArgumentException e) { threw = true; }
        check(threw, "no 1-coin: greedy strands 2 although 3 + 3 works");
        eq(2, fewest(new int[] {3, 4}, 6), "3 + 3 without a 1-coin");
        for (int[] bad : new int[][] {{0, 1}, {5, -1}}) {
            threw = false;
            try { CoinChange.greedy(bad, 7); } catch (IllegalArgumentException e) { threw = true; }
            check(threw, "coin values must be positive: " + Arrays.toString(bad));
        }
        int[] visual = {1, 5, 6};                       // the coin change visualization's coins
        eq(List.of(6, 5), CoinChange.greedy(visual, 11), "greedy finds 6 + 5 for 11");
        eq(2, fewest(visual, 11), "two coins is optimal for 11");
        eq(List.of(6, 1, 1, 1, 1), CoinChange.greedy(visual, 10), "greedy uses five coins for 10");
        eq(2, fewest(visual, 10), "5 + 5 uses two coins for 10");
    }

    /** Fewest coins by breadth-first search over amounts; -1 if impossible. */
    static int fewest(int[] coins, int amount) {
        int[] dist = new int[amount + 1];
        Arrays.fill(dist, -1);
        dist[0] = 0;
        ArrayDeque<Integer> q = new ArrayDeque<>();
        q.add(0);
        while (!q.isEmpty()) {
            int x = q.poll();
            for (int c : coins) {
                int y = x + c;
                if (y <= amount && dist[y] < 0) { dist[y] = dist[x] + 1; q.add(y); }
            }
        }
        return dist[amount];
    }
}
