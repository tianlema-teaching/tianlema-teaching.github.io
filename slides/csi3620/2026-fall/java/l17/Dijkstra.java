package l17;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/** Dijkstra's algorithm with java.util.PriorityQueue and lazy deletion. Requires weights >= 0. */
public final class Dijkstra {
    /** A queue entry: vertex v was reached with tentative distance dist when the entry was added. */
    record Entry(int vertex, long dist) {
    }

    private Dijkstra() {
    }

    public static ShortestPaths run(WeightedDigraph g, int source) {
        return run(g, source, null);
    }

    public static ShortestPaths run(WeightedDigraph g, int source, List<String> trace) {
        int n = g.vertexCount();
        long[] dist = new long[n];
        Arrays.fill(dist, ShortestPaths.INF);
        int[] parent = new int[n];
        Arrays.fill(parent, -1);
        boolean[] done = new boolean[n];
        PriorityQueue<Entry> pq = new PriorityQueue<>(
                Comparator.comparingLong(Entry::dist)
                        .thenComparingInt(Entry::vertex));
        dist[source] = 0;
        pq.add(new Entry(source, 0));
        while (!pq.isEmpty()) {
            Entry top = pq.remove();
            int u = top.vertex();
            if (done[u]) {
                log(trace, g, "skip done " + g.name(u) + "=" + top.dist(), dist, pq);
                continue; // stale entry: u was finalized earlier
            }
            done[u] = true;
            for (WeightedDigraph.Edge e : g.edgesFrom(u)) {
                if (dist[u] + e.weight() < dist[e.to()]) {
                    dist[e.to()] = dist[u] + e.weight();
                    parent[e.to()] = u;
                    pq.add(new Entry(e.to(), dist[e.to()]));
                }
            }
            log(trace, g, "finalize " + g.name(u) + "=" + dist[u], dist, pq);
        }
        return new ShortestPaths(dist, parent);
    }

    private static void log(List<String> trace, WeightedDigraph g, String event, long[] dist, PriorityQueue<Entry> pq) {
        if (trace == null) {
            return;
        }
        List<Entry> sorted = new ArrayList<>(pq);
        sorted.sort(pq.comparator());
        StringBuilder sb = new StringBuilder(event).append(" | dist");
        for (int v = 0; v < dist.length; v++) {
            sb.append(' ').append(g.name(v)).append('=').append(dist[v] == ShortestPaths.INF ? "inf" : dist[v]);
        }
        sb.append(" | pq");
        for (Entry x : sorted) {
            sb.append(" (").append(g.name(x.vertex())).append(',').append(x.dist()).append(')');
        }
        trace.add(sb.toString());
    }
}
