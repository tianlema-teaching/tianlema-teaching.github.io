package l20;

/** Levenshtein distance: fewest insertions, deletions and substitutions turning x into y. */
public final class EditDistance {
    private EditDistance() { }

    /** d[i][j] = edit distance between x[0..i-1] and y[0..j-1]. */
    public static int[][] table(String x, String y) {
        int m = x.length(), n = y.length();
        int[][] d = new int[m + 1][n + 1];
        for (int i = 0; i <= m; i++) d[i][0] = i;   // delete all i characters
        for (int j = 0; j <= n; j++) d[0][j] = j;   // insert all j characters
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                int sub = x.charAt(i - 1) == y.charAt(j - 1) ? 0 : 1;
                d[i][j] = Math.min(d[i - 1][j - 1] + sub,          // match or substitute
                          Math.min(d[i - 1][j] + 1, d[i][j - 1] + 1)); // delete, insert
            }
        }
        return d;
    }

    public static int distance(String x, String y) {
        return table(x, y)[x.length()][y.length()];
    }

    /** Checker: plain exponential recursion on suffixes. */
    static int slow(String x, String y) {
        if (x.isEmpty()) return y.length();
        if (y.isEmpty()) return x.length();
        int sub = x.charAt(0) == y.charAt(0) ? 0 : 1;
        return Math.min(slow(x.substring(1), y.substring(1)) + sub,
               Math.min(slow(x.substring(1), y) + 1, slow(x, y.substring(1)) + 1));
    }
}
