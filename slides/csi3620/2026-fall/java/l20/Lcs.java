package l20;

/** Longest common subsequence of two strings. */
public final class Lcs {
    private Lcs() { }

    /** len[i][j] = LCS length of the prefixes x[0..i-1] and y[0..j-1]. */
    public static int[][] table(String x, String y) {
        int m = x.length(), n = y.length();
        int[][] len = new int[m + 1][n + 1];        // row 0 and column 0: empty prefix
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (x.charAt(i - 1) == y.charAt(j - 1)) {
                    len[i][j] = len[i - 1][j - 1] + 1;  // last characters match
                } else {
                    len[i][j] = Math.max(len[i - 1][j], len[i][j - 1]);
                }
            }
        }
        return len;
    }

    /** One longest common subsequence, traced back from len[m][n]. */
    public static String traceback(int[][] len, String x, String y) {
        StringBuilder out = new StringBuilder();
        int i = x.length(), j = y.length();
        while (i > 0 && j > 0) {
            if (x.charAt(i - 1) == y.charAt(j - 1)) {
                out.append(x.charAt(i - 1));        // part of the answer: go diagonal
                i--;
                j--;
            } else if (len[i - 1][j] >= len[i][j - 1]) {
                i--;                                // up (ties go up)
            } else {
                j--;                                // left
            }
        }
        return out.reverse().toString();
    }

    public static String lcs(String x, String y) {
        return traceback(table(x, y), x, y);
    }

    /** Checker: is s a subsequence of t? */
    static boolean isSubsequence(String s, String t) {
        int k = 0;
        for (int i = 0; i < t.length() && k < s.length(); i++) {
            if (t.charAt(i) == s.charAt(k)) k++;
        }
        return k == s.length();
    }
}
