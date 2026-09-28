package l17;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/** Bellman-Ford: relax every edge up to V-1 times; one more pass detects a reachable negative cycle. */
public final class BellmanFord {
    private BellmanFord() {
    }

    /** Shortest paths from source, or empty if a negative cycle is reachable from source. */
    public static Optional<ShortestPaths> run(WeightedDigraph g, int source) {
        return run(g, source, null);
    }

    public static Optional<ShortestPaths> run(WeightedDigraph g, int source, List<String> trace) {
        int n = g.vertexCount();
        long[] dist = new long[n];
        Arrays.fill(dist, ShortestPaths.INF);
        int[] parent = new int[n];
        Arrays.fill(parent, -1);
        dist[source] = 0;
        for (int pass = 1; pass <= n - 1; pass++) {
            boolean changed = false;
            for (WeightedDigraph.Edge e : g.edges()) {
                if (relax(e, dist, parent)) {
                    changed = true;
                }
            }
            if (trace != null) {
                trace.add("pass " + pass + " dist=" + show(dist) + " changed=" + changed);
            }
            if (!changed) {
                break; // no change: every later pass would do nothing too
            }
        }
        for (WeightedDigraph.Edge e : g.edges()) {
            if (dist[e.from()] != ShortestPaths.INF
                    && dist[e.from()] + e.weight() < dist[e.to()]) {
                return Optional.empty(); // still improvable: negative cycle
            }
        }
        return Optional.of(new ShortestPaths(dist, parent));
    }

    /** Relaxes edge u -> v: if going through u is shorter, update v. Returns true on a change. */
    static boolean relax(WeightedDigraph.Edge e, long[] dist, int[] parent) {
        int u = e.from();
        int v = e.to();
        if (dist[u] != ShortestPaths.INF && dist[u] + e.weight() < dist[v]) {
            dist[v] = dist[u] + e.weight();
            parent[v] = u;
            return true;
        }
        return false;
    }

    static String show(long[] dist) {
        StringBuilder sb = new StringBuilder("[");
        for (int v = 0; v < dist.length; v++) {
            sb.append(v == 0 ? "" : ", ").append(dist[v] == ShortestPaths.INF ? "inf" : String.valueOf(dist[v]));
        }
        return sb.append(']').toString();
    }
}
