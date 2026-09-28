package l16;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An undirected graph. new Graph(n) is simple: addEdge rejects self-loops and parallel edges.
 * Graph.multigraph(n) allows both; Checks uses it to test the cycle detectors on multigraphs.
 */
public final class Graph {
    private final List<List<Integer>> adj = new ArrayList<>();
    private final List<int[]> edges = new ArrayList<>();
    private final boolean simple;

    public Graph(int n) {
        this(n, true);
    }

    private Graph(int n, boolean simple) {
        this.simple = simple;
        for (int v = 0; v < n; v++) {
            adj.add(new ArrayList<>());
        }
    }

    /** A graph that accepts self-loops and parallel edges. A self-loop u-u appears twice in u's list. */
    static Graph multigraph(int n) {
        return new Graph(n, false);
    }

    public void addEdge(int u, int v) {
        if (simple && (u == v || adj.get(u).contains(v))) {
            throw new IllegalArgumentException("simple graphs only: " + u + "-" + v);
        }
        adj.get(u).add(v);
        adj.get(v).add(u);
        edges.add(new int[] {u, v});
    }

    public int vertexCount() {
        return adj.size();
    }

    public List<Integer> neighbors(int u) {
        return Collections.unmodifiableList(adj.get(u));
    }

    public List<int[]> edges() {
        return Collections.unmodifiableList(edges);
    }
}
