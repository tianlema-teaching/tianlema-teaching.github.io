package l21;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Place n queens on an n-by-n board so that no two attack each other. */
public final class NQueens {
    private final int n;
    private final int[] col;                        // col[r] = column of the queen in row r
    private final boolean[] colUsed, diagUsed, antiUsed;
    private final List<int[]> solutions = new ArrayList<>();
    private long nodes;                             // calls to place, including the root
    private final List<String> log;                 // step log for small traces, or null

    private NQueens(int n, boolean logging) {
        if (n < 0) throw new IllegalArgumentException("n must be non-negative: " + n);
        this.n = n;
        col = new int[n];
        colUsed = new boolean[n];
        int lines = Math.max(0, 2 * n - 1);         // n = 0: no lines, one empty solution
        diagUsed = new boolean[lines];              // r - c + n - 1 is constant on a diagonal
        antiUsed = new boolean[lines];              // r + c is constant on an anti-diagonal
        log = logging ? new ArrayList<>() : null;
    }

    private boolean safe(int r, int c) {
        return !colUsed[c] && !diagUsed[r - c + n - 1] && !antiUsed[r + c];
    }

    /** safe(r, c), plus a log entry when the square is rejected. */
    private boolean allowed(int r, int c) {
        boolean ok = safe(r, c);
        if (!ok && log != null) log.add("reject row " + r + " col " + c);
        return ok;
    }

    private void mark(int r, int c, boolean value) {
        colUsed[c] = value;
        diagUsed[r - c + n - 1] = value;
        antiUsed[r + c] = value;
    }

    private void place(int r) {
        nodes++;
        if (r == n) {                               // all rows filled: a solution
            solutions.add(col.clone());
            if (log != null) log.add("solution " + Arrays.toString(col));
            return;
        }
        for (int c = 0; c < n; c++) {
            if (!allowed(r, c)) continue;           // prune: attacked square
            col[r] = c;                             // choose
            mark(r, c, true);
            if (log != null) log.add("place row " + r + " col " + c);
            place(r + 1);                           // explore
            mark(r, c, false);                      // unchoose
            if (log != null) log.add("remove row " + r + " col " + c);
        }
    }

    /** All solutions for an n-by-n board, each as the column of the queen in rows 0..n-1. */
    public static List<int[]> solve(int n) {
        NQueens q = new NQueens(n, false);
        q.place(0);
        return q.solutions;
    }

    /** Number of search-tree nodes (calls to place) visited with pruning. */
    public static long nodesWithPruning(int n) {
        NQueens q = new NQueens(n, false);
        q.place(0);
        return q.nodes;
    }

    /** Step log of the full pruned search, for tracing small boards. */
    public static List<String> trace(int n) {
        NQueens q = new NQueens(n, true);
        q.place(0);
        return q.log;
    }

    /** Without pruning: one queen per row in any column, checked only when all n are placed. */
    public static long[] withoutPruning(int n) {
        long[] counts = new long[2];                // {nodes visited, solutions}
        blind(new int[n], 0, counts);
        return counts;
    }

    private static void blind(int[] cols, int r, long[] counts) {
        counts[0]++;
        if (r == cols.length) {
            if (valid(cols)) counts[1]++;
            return;
        }
        for (int c = 0; c < cols.length; c++) {
            cols[r] = c;
            blind(cols, r + 1, counts);
        }
    }

    /** One queen per row and per column (the permutation tree), checked only when full. */
    public static long[] columnsOnly(int n) {
        long[] counts = new long[2];                // {nodes visited, solutions}
        permute(new int[n], new boolean[n], 0, counts);
        return counts;
    }

    private static void permute(int[] cols, boolean[] used, int r, long[] counts) {
        counts[0]++;
        if (r == cols.length) {
            if (valid(cols)) counts[1]++;
            return;
        }
        for (int c = 0; c < cols.length; c++) {
            if (used[c]) continue;                  // each column once
            used[c] = true;
            cols[r] = c;
            permute(cols, used, r + 1, counts);
            used[c] = false;
        }
    }

    /** True if no two queens (one per row) share a column or a diagonal. */
    static boolean valid(int[] cols) {
        for (int a = 0; a < cols.length; a++) {
            for (int b = a + 1; b < cols.length; b++) {
                if (cols[a] == cols[b] || Math.abs(cols[a] - cols[b]) == b - a) return false;
            }
        }
        return true;
    }
}
