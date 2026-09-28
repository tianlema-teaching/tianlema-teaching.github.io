package l20;

import java.util.Arrays;

/** Four ways to compute Fibonacci numbers, with F(0) = 0 and F(1) = 1. */
public final class Fibonacci {
    private Fibonacci() { }

    private static long calls;   // calls made by the most recent counted run

    /** Plain recursion: solves the same subproblems again and again. */
    public static long naive(int n) {
        calls++;
        if (n <= 1) return n;                       // F(0) = 0, F(1) = 1
        return naive(n - 1) + naive(n - 2);
    }

    /** Number of calls naive(n) makes, counted by running it. */
    public static long naiveCalls(int n) {
        calls = 0;
        naive(n);
        return calls;
    }

    /** Top-down: recursion plus a memo of answers already computed. */
    public static long memo(int n) {
        long[] memo = new long[n + 1];
        Arrays.fill(memo, -1);                      // -1 means "not computed yet"
        return memo(n, memo);
    }

    private static long memo(int n, long[] memo) {
        calls++;
        if (n <= 1) return n;
        if (memo[n] != -1) return memo[n];          // reuse a stored answer
        memo[n] = memo(n - 1, memo) + memo(n - 2, memo);
        return memo[n];
    }

    /** Number of calls memo(n) makes, counted by running it. */
    public static long memoCalls(int n) {
        calls = 0;
        memo(n);
        return calls;
    }

    /** Bottom-up: fill f[0..n] from the base cases upward. */
    public static long table(int n) {
        if (n <= 1) return n;
        long[] f = new long[n + 1];
        f[0] = 0;
        f[1] = 1;
        for (int i = 2; i <= n; i++) {
            f[i] = f[i - 1] + f[i - 2];
        }
        return f[n];
    }

    /** Bottom-up keeping only the last two values: O(1) extra space. */
    public static long twoVariables(int n) {
        if (n == 0) return 0;
        long prev = 0, curr = 1;                    // F(i-1), F(i) for i = 1
        for (int i = 2; i <= n; i++) {
            long next = prev + curr;
            prev = curr;
            curr = next;
        }
        return curr;
    }
}
