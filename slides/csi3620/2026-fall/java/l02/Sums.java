package l02;

/** Two correct ways to compute 1 + 2 + ... + n for n >= 0, and a recursive third. */
public final class Sums {
    private Sums() { }

    public static long sumLoop(int n) {
        long total = 0;
        for (long i = 1; i <= n; i++) {
            total += i;                // n additions
        }
        return total;
    }

    public static long sumFormula(int n) {
        return (long) n * (n + 1L) / 2; // a fixed number of operations
    }

    public static long sumRecursive(int n) {
        if (n <= 0) {
            return 0;
        }
        return n + sumRecursive(n - 1); // n + 1 frames are live at the bottom
    }
}
