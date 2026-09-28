package l15;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

/** Breadth-first search from one source: O(V + E) with adjacency lists. */
public final class Bfs {
    public final int[] dist;              // edges from the source; -1 if unreachable
    public final int[] parent;            // BFS tree parent; -1 for the source and unreachable
    public final List<Integer> order = new ArrayList<>();   // dequeue order

    public Bfs(Graph g, int s) {
        int n = g.vertexCount();
        dist = new int[n];
        parent = new int[n];
        Arrays.fill(dist, -1);
        Arrays.fill(parent, -1);
        boolean[] discovered = new boolean[n];
        Queue<Integer> queue = new ArrayDeque<>();
        discovered[s] = true;             // mark when enqueued
        dist[s] = 0;
        queue.add(s);
        while (!queue.isEmpty()) {
            int u = queue.remove();
            order.add(u);
            for (int v : g.neighbors(u)) {
                if (!discovered[v]) {
                    discovered[v] = true;
                    dist[v] = dist[u] + 1;
                    parent[v] = u;
                    queue.add(v);
                }
            }
        }
    }

    /** A shortest path (fewest edges) from the source to v, or an empty list. */
    public List<Integer> pathTo(int v) {
        if (dist[v] == -1) return List.of();
        List<Integer> path = new ArrayList<>();
        for (int x = v; x != -1; x = parent[x]) path.add(x);
        Collections.reverse(path);
        return path;
    }
}
