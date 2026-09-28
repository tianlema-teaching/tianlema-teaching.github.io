package l20;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Coin problems on an unlimited supply of each denomination. */
public final class CoinChange {
    private CoinChange() { }

    static final int INF = Integer.MAX_VALUE;       // "no way to make this amount"

    /** best[a] = fewest coins summing to a, or INF if a cannot be made. */
    public static int[] fewestTable(int[] coins, int amount) {
        int[] best = new int[amount + 1];
        Arrays.fill(best, INF);
        best[0] = 0;                                // zero coins make amount 0
        for (int a = 1; a <= amount; a++) {
            for (int c : coins) {
                if (c <= a && best[a - c] != INF) {
                    best[a] = Math.min(best[a], best[a - c] + 1);
                }
            }
        }
        return best;
    }

    /** Fewest coins summing to amount, or -1 if impossible. */
    public static int fewest(int[] coins, int amount) {
        int best = fewestTable(coins, amount)[amount];
        return best == INF ? -1 : best;
    }

    /** One optimal multiset of coins, found by walking the table back from amount. */
    public static List<Integer> fewestCoins(int[] coins, int amount) {
        int[] best = fewestTable(coins, amount);
        if (best[amount] == INF) return null;
        List<Integer> used = new ArrayList<>();
        int a = amount;
        while (a > 0) {
            for (int c : coins) {
                if (c <= a && best[a - c] != INF && best[a - c] + 1 == best[a]) {
                    used.add(c);                    // c was a last coin of some optimum
                    a -= c;
                    break;
                }
            }
        }
        return used;
    }

    /** Number of multisets of coins summing to amount (order does not matter). */
    public static long countWays(int[] coins, int amount) {
        long[] ways = new long[amount + 1];
        ways[0] = 1;                                // the empty multiset
        for (int c : coins) {  // coins outside: each multiset once
            for (int a = c; a <= amount; a++) {
                ways[a] += ways[a - c];
            }
        }
        return ways[amount];
    }

    /** Number of ordered sequences of coins summing to amount (order matters). */
    public static long countSequences(int[] coins, int amount) {
        long[] ways = new long[amount + 1];
        ways[0] = 1;
        for (int a = 1; a <= amount; a++) {         // amounts outside: every order
            for (int c : coins) {
                if (c <= a) ways[a] += ways[a - c];
            }
        }
        return ways[amount];
    }

    /** Largest-coin-first greedy count, or -1 if it gets stuck (coins sorted descending). */
    public static int greedy(int[] coinsDescending, int amount) {
        int count = 0;
        for (int c : coinsDescending) {
            count += amount / c;
            amount %= c;
        }
        return amount == 0 ? count : -1;
    }
}
