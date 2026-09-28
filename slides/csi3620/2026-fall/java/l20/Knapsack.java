package l20;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Knapsack with integer weights: 0/1 (each item once) and unbounded (any number of copies). */
public final class Knapsack {
    private Knapsack() { }

    /** dp[i][c] = best value using only items 0..i-1 with capacity c. */
    public static int[][] table(int[] w, int[] v, int cap) {
        int n = w.length;
        int[][] dp = new int[n + 1][cap + 1];       // row 0: no items, value 0
        for (int i = 1; i <= n; i++) {
            for (int c = 0; c <= cap; c++) {
                dp[i][c] = dp[i - 1][c];            // skip item i-1
                if (w[i - 1] <= c) {                // or take it, if it fits
                    int take = dp[i - 1][c - w[i - 1]] + v[i - 1];
                    dp[i][c] = Math.max(dp[i][c], take);
                }
            }
        }
        return dp;
    }

    /** Indices of one optimal set of items, read back from the table. */
    public static List<Integer> chosen(int[][] dp, int[] w, int cap) {
        List<Integer> items = new ArrayList<>();
        int c = cap;
        for (int i = dp.length - 1; i >= 1; i--) {
            if (dp[i][c] != dp[i - 1][c]) {  // changed: item i-1 was taken
                items.add(i - 1);
                c -= w[i - 1];
            }
        }
        Collections.reverse(items);
        return items;
    }

    /** 0/1 knapsack in one row: capacities run downward so each item is used at most once. */
    public static int oneRow(int[] w, int[] v, int cap) {
        int[] best = new int[cap + 1];
        for (int i = 0; i < w.length; i++) {
            // downward: best[c - w[i]] is still old
            for (int c = cap; c >= w[i]; c--) {
                best[c] = Math.max(best[c], best[c - w[i]] + v[i]);
            }
        }
        return best[cap];
    }

    /** Unbounded knapsack in one row: capacities run upward so an item can repeat. */
    public static int unbounded(int[] w, int[] v, int cap) {
        int[] best = new int[cap + 1];
        for (int i = 0; i < w.length; i++) {
            // upward: best[c - w[i]] may include item i
            for (int c = w[i]; c <= cap; c++) {
                best[c] = Math.max(best[c], best[c - w[i]] + v[i]);
            }
        }
        return best[cap];
    }

    /** Checker: try all 2^n subsets. */
    static int bruteForce(int[] w, int[] v, int cap) {
        int best = 0;
        for (int mask = 0; mask < (1 << w.length); mask++) {
            int weight = 0, value = 0;
            for (int i = 0; i < w.length; i++) {
                if ((mask & (1 << i)) != 0) { weight += w[i]; value += v[i]; }
            }
            if (weight <= cap) best = Math.max(best, value);
        }
        return best;
    }
}
