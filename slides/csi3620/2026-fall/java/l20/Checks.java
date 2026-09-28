package l20;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/** Concrete checks for every class in package l20. Prints "l20 OK" when all pass. */
public final class Checks {
    private Checks() { }

    static void check(boolean ok, String what) {
        if (!ok) throw new AssertionError(what);
    }

    static void fibonacci() {
        long[] expected = {0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55};
        for (int n = 0; n <= 10; n++) {
            check(Fibonacci.naive(n) == expected[n], "naive F(" + n + ")");
            check(Fibonacci.memo(n) == expected[n], "memo F(" + n + ")");
            check(Fibonacci.table(n) == expected[n], "table F(" + n + ")");
            check(Fibonacci.twoVariables(n) == expected[n], "two variables F(" + n + ")");
        }
        // Naive call count is 2 F(n+1) - 1, checked against the counted runs.
        for (int n = 0; n <= 25; n++) {
            check(Fibonacci.naiveCalls(n) == 2 * Fibonacci.table(n + 1) - 1, "naive calls " + n);
        }
        check(Fibonacci.naiveCalls(5) == 15, "15 calls for n = 5");
        check(Fibonacci.naiveCalls(30) == 2_692_537, "calls for n = 30");
        check(Fibonacci.naiveCalls(20) == 21_891, "calls for n = 20");
        check(Fibonacci.naiveCalls(40) == 331_160_281L, "calls for n = 40");
        long[] byArg = new long[6];
        countArguments(5, byArg);
        check(Arrays.equals(byArg, new long[] {3, 5, 3, 2, 1, 1}), "naive(5) calls per argument 0..5");
        long[] chart = {1, 1, 3, 5, 9, 15, 25, 41, 67, 109, 177};
        for (int n = 0; n <= 10; n++) check(Fibonacci.naiveCalls(n) == chart[n], "chart value " + n);
        for (int n = 1; n <= 40; n++) {
            check(Fibonacci.memoCalls(n) == 2L * n - 1, "memo calls " + n);
        }
        check(Fibonacci.memoCalls(0) == 1, "memo calls 0");
        check(Fibonacci.twoVariables(90) == 2_880_067_194_370_816_120L, "F(90)");
        check(Fibonacci.table(92) == Fibonacci.twoVariables(92), "F(92) agrees");
    }

    /** Checker: how many times plain recursion is called with each argument. */
    static void countArguments(int n, long[] byArg) {
        byArg[n]++;
        if (n <= 1) return;
        countArguments(n - 1, byArg);
        countArguments(n - 2, byArg);
    }

    /** Checker for coins: plain recursion over the last coin. */
    static int slowFewest(int[] coins, int a) {
        if (a == 0) return 0;
        int best = -1;
        for (int c : coins) {
            if (c <= a) {
                int rest = slowFewest(coins, a - c);
                if (rest >= 0 && (best < 0 || rest + 1 < best)) best = rest + 1;
            }
        }
        return best;
    }

    static void coins() {
        int[] coins = {1, 3, 4};
        int[] best = CoinChange.fewestTable(coins, 6);
        check(Arrays.equals(best, new int[] {0, 1, 2, 1, 1, 2, 2}), "fewest table");
        check(CoinChange.fewest(coins, 6) == 2, "fewest 6");
        check(CoinChange.fewestCoins(coins, 6).equals(List.of(3, 3)), "3 + 3");
        check(CoinChange.greedy(new int[] {4, 3, 1}, 6) == 3, "greedy uses 3 coins");
        check(CoinChange.countWays(coins, 6) == 4, "4 multisets");
        check(CoinChange.countSequences(coins, 6) == 9, "9 sequences");
        int[][] prefixes = {{1}, {1, 3}, {1, 3, 4}};
        long[][] waysAfter = {{1, 1, 1, 1, 1, 1, 1}, {1, 1, 1, 2, 2, 2, 3}, {1, 1, 1, 2, 3, 3, 4}};
        for (int k = 0; k < 3; k++) {
            for (int a = 0; a <= 6; a++) {                          // ways[a] after coins 1..k
                check(CoinChange.countWays(prefixes[k], a) == waysAfter[k][a], "ways array " + k + " " + a);
            }
        }
        check(CoinChange.fewest(new int[] {5, 7}, 3) == -1, "impossible");
        check(CoinChange.fewestCoins(new int[] {5, 7}, 3) == null, "impossible coins");
        check(CoinChange.fewest(coins, 0) == 0, "amount 0");
        check(CoinChange.countWays(coins, 0) == 1, "one way to make 0");
        check(CoinChange.countWays(new int[] {1, 2, 5}, 5) == 4, "1,2,5 make 5 in 4 ways");
        check(CoinChange.countSequences(new int[] {1, 2}, 4) == 5, "sequences are Fibonacci");
        Random rnd = new Random(20);
        for (int t = 0; t < 200; t++) {
            int[] cs = {1 + rnd.nextInt(6), 1 + rnd.nextInt(9), 2 + rnd.nextInt(9)};
            int a = rnd.nextInt(25);
            check(CoinChange.fewest(cs, a) == slowFewest(cs, a), "random fewest");
            List<Integer> used = CoinChange.fewestCoins(cs, a);
            if (used != null) {
                check(used.size() == CoinChange.fewest(cs, a), "reconstruction size");
                check(used.stream().mapToInt(Integer::intValue).sum() == a, "reconstruction sum");
            }
        }
    }

    static void knapsack() {
        int[] w = {1, 3, 4};
        int[] v = {1, 5, 6};
        int[][] dp = Knapsack.table(w, v, 6);
        check(Arrays.deepEquals(dp, new int[][] {
            {0, 0, 0, 0, 0, 0, 0},
            {0, 1, 1, 1, 1, 1, 1},
            {0, 1, 1, 5, 6, 6, 6},
            {0, 1, 1, 5, 6, 7, 7}}), "knapsack table");
        check(Knapsack.chosen(dp, w, 6).equals(List.of(0, 2)), "items A and C");
        check(Knapsack.oneRow(w, v, 6) == 7, "one row 0/1");
        check(Knapsack.unbounded(w, v, 6) == 10, "unbounded takes B twice");
        check(Knapsack.table(new int[0], new int[0], 5)[0][5] == 0, "no items");
        check(Knapsack.oneRow(w, v, 0) == 0, "capacity 0");
        int[] downB = {0, 0, 0, 5, 5, 5, 5}, upB = {0, 0, 0, 5, 5, 5, 10};  // item B alone
        for (int c = 0; c <= 6; c++) {
            check(Knapsack.oneRow(new int[] {3}, new int[] {5}, c) == downB[c], "B downward " + c);
            check(Knapsack.unbounded(new int[] {3}, new int[] {5}, c) == upB[c], "B upward " + c);
        }
        Random rnd = new Random(7);
        for (int t = 0; t < 300; t++) {
            int n = rnd.nextInt(7);
            int[] ww = new int[n], vv = new int[n];
            for (int i = 0; i < n; i++) { ww[i] = 1 + rnd.nextInt(8); vv[i] = rnd.nextInt(20); }
            int cap = rnd.nextInt(20);
            int brute = Knapsack.bruteForce(ww, vv, cap);
            int[][] tab = Knapsack.table(ww, vv, cap);
            check(tab[n][cap] == brute, "table vs brute");
            check(Knapsack.oneRow(ww, vv, cap) == brute, "one row vs brute");
            int weight = 0, value = 0;
            for (int i : Knapsack.chosen(tab, ww, cap)) { weight += ww[i]; value += vv[i]; }
            check(weight <= cap && value == brute, "chosen items are optimal");
            check(Knapsack.unbounded(ww, vv, cap) >= brute, "unbounded at least 0/1");
        }
    }

    static void lcs() {
        int[][] len = Lcs.table("BACDB", "BDCB");
        check(Arrays.deepEquals(len, new int[][] {
            {0, 0, 0, 0, 0},
            {0, 1, 1, 1, 1},
            {0, 1, 1, 1, 1},
            {0, 1, 1, 2, 2},
            {0, 1, 2, 2, 2},
            {0, 1, 2, 2, 3}}), "LCS table for BACDB and BDCB");
        check(len[5][4] == 3, "LCS length 3");
        check(Lcs.lcs("BACDB", "BDCB").equals("BCB"), "LCS BCB");
        check(Lcs.lcs("", "ABC").isEmpty(), "empty");
        check(Lcs.lcs("ABC", "XYZ").isEmpty(), "nothing shared");
        check(Lcs.lcs("SAME", "SAME").equals("SAME"), "identical");
        Random rnd = new Random(3);
        for (int t = 0; t < 300; t++) {
            String x = randomWord(rnd, 7), y = randomWord(rnd, 7);
            String s = Lcs.lcs(x, y);
            check(Lcs.isSubsequence(s, x) && Lcs.isSubsequence(s, y), "common");
            check(s.length() == bruteLcs(x, y), "longest");
        }
    }

    static String randomWord(Random rnd, int maxLen) {
        StringBuilder sb = new StringBuilder();
        int n = rnd.nextInt(maxLen + 1);
        for (int i = 0; i < n; i++) sb.append((char) ('A' + rnd.nextInt(3)));
        return sb.toString();
    }

    /** Checker: longest subsequence of x (all 2^m of them) that is also in y. */
    static int bruteLcs(String x, String y) {
        int best = 0;
        for (int mask = 0; mask < (1 << x.length()); mask++) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < x.length(); i++) if ((mask & (1 << i)) != 0) sb.append(x.charAt(i));
            if (Lcs.isSubsequence(sb.toString(), y)) best = Math.max(best, sb.length());
        }
        return best;
    }

    static void editDistance() {
        check(Arrays.deepEquals(EditDistance.table("CART", "CHAT"), new int[][] {
            {0, 1, 2, 3, 4},
            {1, 0, 1, 2, 3},
            {2, 1, 1, 1, 2},
            {3, 2, 2, 2, 2},
            {4, 3, 3, 3, 2}}), "edit table CART to CHAT");
        check(EditDistance.distance("CART", "CHAT") == 2, "CART to CHAT");
        check(EditDistance.distance("", "ABC") == 3, "insert all");
        check(EditDistance.distance("ABC", "") == 3, "delete all");
        check(EditDistance.distance("SAME", "SAME") == 0, "identical");
        Random rnd = new Random(11);
        for (int t = 0; t < 300; t++) {
            String x = randomWord(rnd, 6), y = randomWord(rnd, 6);
            check(EditDistance.distance(x, y) == EditDistance.slow(x, y), "table vs recursion");
        }
    }

    public static void main(String[] args) {
        fibonacci();
        coins();
        knapsack();
        lcs();
        editDistance();
        System.out.println("l20 OK");
    }
}
