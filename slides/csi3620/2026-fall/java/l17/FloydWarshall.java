package l17;

import java.util.Arrays;
import java.util.List;

/** Floyd-Warshall all-pairs shortest paths: O(V^3) time, O(V^2) space. */
public final class FloydWarshall {
    private FloydWarshall() {
    }

    public static long[][] run(WeightedDigraph g) {
        return run(g, null);
    }

    public static long[][] run(WeightedDigraph g, List<String> trace) {
        int n = g.vertexCount();
        long[][] d = new long[n][n];
        for (int i = 0; i < n; i++) {
            Arrays.fill(d[i], ShortestPaths.INF);
            d[i][i] = 0;
        }
        for (WeightedDigraph.Edge e : g.edges()) {
            d[e.from()][e.to()] = Math.min(d[e.from()][e.to()], e.weight());
        }
        if (trace != null) {
            trace.add("D0 " + show(d));
        }
        // With a negative cycle, entries keep falling: a new entry can be the sum of two negative
        // entries, so values can shrink quickly and could overflow a long on a large graph.
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (d[i][k] != ShortestPaths.INF && d[k][j] != ShortestPaths.INF
                            && d[i][k] + d[k][j] < d[i][j]) {
                        d[i][j] = d[i][k] + d[k][j];
                    }
                }
            }
            if (trace != null) {
                trace.add("after k=" + k + " " + show(d));
            }
        }
        return d;
    }

    /**
     * After run: some d[i][i] < 0 exactly when the graph has a negative cycle. Every vertex on a
     * simple negative cycle gets a negative diagonal entry, but a negative d[i][i] alone does not
     * prove that i itself lies on a simple negative cycle (see Checks.negativeCycle).
     */
    public static boolean hasNegativeCycle(long[][] d) {
        for (int i = 0; i < d.length; i++) {
            if (d[i][i] < 0) {
                return true;
            }
        }
        return false;
    }

    static String show(long[][] d) {
        StringBuilder sb = new StringBuilder();
        for (long[] row : d) {
            sb.append(BellmanFord.show(row));
        }
        return sb.toString();
    }
}
