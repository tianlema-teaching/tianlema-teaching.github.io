package l17;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

/** BFS distances: the shortest paths when every edge counts as 1 (weights are ignored). */
public final class Bfs {
    private Bfs() {
    }

    public static ShortestPaths edgeCounts(WeightedDigraph g, int source) {
        long[] dist = new long[g.vertexCount()];
        Arrays.fill(dist, ShortestPaths.INF);
        int[] parent = new int[g.vertexCount()];
        Arrays.fill(parent, -1);
        Deque<Integer> queue = new ArrayDeque<>();
        dist[source] = 0;
        queue.add(source);
        while (!queue.isEmpty()) {
            int u = queue.remove();
            for (WeightedDigraph.Edge e : g.edgesFrom(u)) {
                if (dist[e.to()] == ShortestPaths.INF) {
                    dist[e.to()] = dist[u] + 1;
                    parent[e.to()] = u;
                    queue.add(e.to());
                }
            }
        }
        return new ShortestPaths(dist, parent);
    }
}
