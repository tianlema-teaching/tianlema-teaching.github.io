package l18;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

/** Dijkstra from a source (L17), kept only to compare its tree with an MST. */
public final class ShortestPathTree {
    private ShortestPathTree() { }

    /** The edges that give each reachable vertex its shortest path from s. */
    public static List<Edge> from(Graph g, int s) {
        long[] dist = new long[g.vertexCount()];
        Edge[] via = new Edge[g.vertexCount()];
        Arrays.fill(dist, Long.MAX_VALUE);
        dist[s] = 0;
        PriorityQueue<long[]> pq = new PriorityQueue<>((a, b) -> Long.compare(a[0], b[0]));
        pq.add(new long[] {0, s});
        while (!pq.isEmpty()) {
            long[] top = pq.poll();
            int u = (int) top[1];
            if (top[0] > dist[u]) continue;
            for (Edge e : g.adjacent(u)) {
                int w = e.other(u);
                if (dist[u] + e.weight() < dist[w]) {
                    dist[w] = dist[u] + e.weight();
                    via[w] = e;
                    pq.add(new long[] {dist[w], w});
                }
            }
        }
        List<Edge> tree = new ArrayList<>();
        for (Edge e : via) if (e != null) tree.add(e);
        return tree;
    }
}
