package l02;

import java.math.BigInteger;
import java.util.Arrays;

public final class Checks {
    private Checks() { }

    static void check(boolean ok, String what) {
        if (!ok) {
            throw new AssertionError(what);
        }
    }

    /** Total key comparisons of insertion sort over every ordering of a[k..]. */
    static long totalOverOrders(int[] a, int k) {
        if (k == a.length) {
            return InsertionSort.sort(a.clone());
        }
        long total = 0;
        for (int i = k; i < a.length; i++) {
            int t = a[k];
            a[k] = a[i];
            a[i] = t;
            total += totalOverOrders(a, k + 1);
            t = a[k];
            a[k] = a[i];
            a[i] = t;
        }
        return total;
    }

    public static void main(String[] args) {
        // Loop counts; the running example is n = 8.
        check(Loops.single(8) == 8, "single 8");
        check(Loops.nested(8) == 64, "nested 64");
        check(Loops.dependent(8) == 28, "dependent 28");
        check(Loops.halving(8) == 4, "halving 8 -> 4 iterations");
        check(Loops.nestedHalving(8) == 32, "n * (log n + 1)");
        for (int n = 1; n <= 2000; n++) {
            check(Loops.single(n) == n, "single");
            check(Loops.dependent(n) == (long) n * (n - 1) / 2, "dependent formula");
            if (n >= 2) {
                check(4 * Loops.dependent(n) >= (long) n * n, "n(n-1)/2 >= n^2/4 for n >= 2");
            }
            check(2 * Loops.dependent(n) <= (long) n * n, "n(n-1)/2 <= n^2/2");
            check(Loops.halving(n) == Growth.log2(n) + 1, "halving = floor(log2 n)+1 at " + n);
        }
        check(Loops.halving(0) == 0 && Loops.dependent(0) == 0, "n = 0");
        check(Loops.halving(1000) == 10 && Loops.halving(1_000_000) == 20, "halving 1000, 1e6");

        // Sums.
        for (int n = 0; n <= 3000; n++) {
            check(Sums.sumLoop(n) == Sums.sumFormula(n), "loop = formula at " + n);
        }
        check(Sums.sumFormula(8) == 36 && Sums.sumRecursive(8) == 36, "sum to 8 is 36");
        check(Sums.sumFormula(100_000) == 5_000_050_000L, "long arithmetic, no overflow");
        check(Sums.sumRecursive(1000) == 500_500, "recursive");
        int m = 46_340;
        check(m * (m + 1) > 0 && (m + 1) * (m + 2) < 0, "int n*(n+1) overflows first at n = 46341");
        check(Sums.sumFormula(46_341) == 1_073_767_311L, "long formula is fine there");
        check(Sums.sumFormula(Integer.MAX_VALUE) == 2_305_843_008_139_952_128L, "formula at Integer.MAX_VALUE");
        check(Sums.sumLoop(50_000_000) == Sums.sumFormula(50_000_000)
                && Sums.sumFormula(50_000_000) == 1_250_000_025_000_000L, "loop = formula at 5e7");
        int big = Integer.MAX_VALUE;
        check((long) big * (big + 1) / 2 != Sums.sumFormula(big), "(long) n * (n + 1) alone is wrong at MAX_VALUE");
        check(Sums.sumRecursive(0) == 0 && Sums.sumRecursive(-1) == 0 && Sums.sumLoop(-1) == 0, "n <= 0 gives 0");

        // Insertion sort best and worst on n = 8.
        int[] sorted = {1, 2, 3, 4, 5, 6, 7, 8};
        int[] reversed = {8, 7, 6, 5, 4, 3, 2, 1};
        check(InsertionSort.sort(sorted) == 7, "sorted input: n - 1 comparisons");
        check(InsertionSort.sort(reversed) == 28, "reversed input: n(n-1)/2 comparisons");
        check(Arrays.equals(reversed, new int[] {1, 2, 3, 4, 5, 6, 7, 8}), "reversed got sorted");
        int[] mixed = {5, 2, 8, 1, 9, 3, 3, 0};
        InsertionSort.sort(mixed);
        check(Arrays.equals(mixed, new int[] {0, 1, 2, 3, 3, 5, 8, 9}), "mixed sorted");
        check(InsertionSort.sort(new int[0]) == 0 && InsertionSort.sort(new int[] {4}) == 0, "tiny");
        for (int n = 2; n <= 200; n++) {
            int[] up = new int[n];
            int[] down = new int[n];
            for (int i = 0; i < n; i++) {
                up[i] = i;
                down[i] = n - i;
            }
            check(InsertionSort.sort(up) == n - 1, "best n-1");
            check(InsertionSort.sort(down) == (long) n * (n - 1) / 2, "worst n(n-1)/2");
        }

        // Average case, n = 8, all 40320 orders of distinct keys equally likely:
        // 777456 / 40320 = 19.28..., that is n(n-1)/4 + n - H_n; the leading term is n^2/4.
        check(totalOverOrders(new int[] {1, 2, 3, 4, 5, 6, 7, 8}, 0) == 777_456L, "average n = 8");

        // Space.
        int[] a = {3, 1, 4, 1, 5};
        check(Arrays.equals(Space.reversedCopy(a), new int[] {5, 1, 4, 1, 3}), "copy");
        check(Arrays.equals(a, new int[] {3, 1, 4, 1, 5}), "copy leaves input");
        Space.reverseInPlace(a);
        check(Arrays.equals(a, new int[] {5, 1, 4, 1, 3}), "in place");
        int[] empty = {};
        Space.reverseInPlace(empty);
        check(Space.reversedCopy(empty).length == 0, "empty");

        // Worked bound: 3n^2 + 5n + 2 = Theta(n^2).
        check(Bounds.f(1) == 10 && Bounds.f(8) == 234, "f values");
        check(Bounds.sandwiched(3, 10, 1, 100_000), "c1=3, c2=10, n0=1");
        check(Bounds.sandwiched(3, 4, 6, 100_000), "c2=4 works from n0=6");
        check(!Bounds.sandwiched(3, 4, 5, 5), "c2=4 fails at n=5");
        check(Bounds.f(5) == 102 && Bounds.f(6) == 140, "f(5)=102 > 100, f(6)=140 <= 144");
        check(!Bounds.sandwiched(4, 10, 1, 100), "c1=4 fails");
        check(!Bounds.sandwiched(3, 9, 1, 1), "c2=9 fails at n=1 (f(1)=10)");
        for (long n = 6; n <= 100_000; n++) {
            check(n * n >= 6 * n && 6 * n >= 5 * n + 6 && 5 * n + 6 > 5 * n + 2, "n^2 > 5n + 2 chain at " + n);
        }

        // Growth table rows.
        check(Growth.log2(8) == 3 && Growth.log2(16) == 4 && Growth.log2(32) == 5 && Growth.log2(1024) == 10, "log2");
        check(Growth.twoTo(8).equals(BigInteger.valueOf(256)), "2^8");
        check(Growth.twoTo(16).equals(BigInteger.valueOf(65_536)), "2^16");
        check(Growth.twoTo(32).equals(BigInteger.valueOf(4_294_967_296L)), "2^32");
        check(Growth.factorial(8).equals(BigInteger.valueOf(40_320)), "8!");
        check(Growth.factorial(16).equals(BigInteger.valueOf(20_922_789_888_000L)), "16!");
        check(Growth.factorial(32).toString().length() == 36, "32! has 36 digits: " + Growth.factorial(32));
        check(Growth.factorial(0).equals(BigInteger.ONE), "0!");

        System.out.println("l02 OK");
    }
}
