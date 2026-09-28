package l15;

import java.util.ArrayList;
import java.util.List;

/** A graph on vertices 0..n-1 stored as adjacency lists. */
public final class Graph {
    private final List<List<Integer>> adj;
    private final boolean directed;
    private int edges;

    public Graph(int n, boolean directed) {
        if (n < 0) throw new IllegalArgumentException("n must be >= 0");
        this.directed = directed;
        adj = new ArrayList<>(n);
        for (int v = 0; v < n; v++) adj.add(new ArrayList<>());
    }

    /** Adds edge u-v (or u->v if directed). Does not check for duplicates. */
    public void addEdge(int u, int v) {
        check(u);
        check(v);
        adj.get(u).add(v);
        if (!directed && u != v) adj.get(v).add(u);
        edges++;
    }

    /** Neighbors of u in insertion order: the out-neighbors if directed. */
    public List<Integer> neighbors(int u) {
        check(u);
        return List.copyOf(adj.get(u));
    }

    /** O(deg(u)): scans u's list. An adjacency matrix answers in O(1). */
    public boolean hasEdge(int u, int v) {
        check(u);
        check(v);
        return adj.get(u).contains(v);
    }

    /** The out-degree if directed. */
    public int degree(int u) {
        check(u);
        return adj.get(u).size();
    }

    public int vertexCount() { return adj.size(); }

    public int edgeCount() { return edges; }

    public boolean isDirected() { return directed; }

    private void check(int v) {
        if (v < 0 || v >= adj.size()) throw new IndexOutOfBoundsException("no vertex " + v);
    }
}
