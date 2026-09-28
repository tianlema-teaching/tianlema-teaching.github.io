package l02;

/** The function from the worked proof, and a checker for claimed constants. */
public final class Bounds {
    private Bounds() { }

    public static long f(long n) {
        return 3 * n * n + 5 * n + 2;
    }

    /** True if c1*n^2 <= f(n) <= c2*n^2 for every n in [n0, limit]. */
    public static boolean sandwiched(long c1, long c2, long n0, long limit) {
        for (long n = n0; n <= limit; n++) {
            long g = n * n;
            if (c1 * g > f(n) || f(n) > c2 * g) {
                return false;
            }
        }
        return true;
    }
}
