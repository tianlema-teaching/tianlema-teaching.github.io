package l17;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/** Single-source result: a distance and a parent (predecessor) for every vertex. */
public final class ShortestPaths {
    public static final long INF = Long.MAX_VALUE;

    private final long[] dist;
    private final int[] parent;

    ShortestPaths(long[] dist, int[] parent) {
        this.dist = dist.clone();
        this.parent = parent.clone();
    }

    public long distTo(int v) {
        return dist[v];
    }

    public boolean hasPathTo(int v) {
        return dist[v] != INF;
    }

    /** Follows parent pointers back from v, then reverses: source first, v last. */
    public List<Integer> pathTo(int v) {
        if (!hasPathTo(v)) {
            return List.of();
        }
        Deque<Integer> path = new ArrayDeque<>();
        for (int x = v; x != -1; x = parent[x]) {
            path.push(x);
        }
        return new ArrayList<>(path);
    }
}
