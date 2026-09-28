package l17;

import java.util.Arrays;

/** A deliberately wrong Floyd-Warshall with k innermost, kept so Checks can show that loop order matters. */
final class Pitfalls {
    private Pitfalls() {
    }

    static long[][] floydWarshallKInnermost(WeightedDigraph g) {
        int n = g.vertexCount();
        long[][] d = new long[n][n];
        for (int i = 0; i < n; i++) {
            Arrays.fill(d[i], ShortestPaths.INF);
            d[i][i] = 0;
        }
        for (WeightedDigraph.Edge e : g.edges()) {
            d[e.from()][e.to()] = Math.min(d[e.from()][e.to()], e.weight());
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    if (d[i][k] != ShortestPaths.INF && d[k][j] != ShortestPaths.INF
                            && d[i][k] + d[k][j] < d[i][j]) {
                        d[i][j] = d[i][k] + d[k][j];
                    }
                }
            }
        }
        return d;
    }
}
