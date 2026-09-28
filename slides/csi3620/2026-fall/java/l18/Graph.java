package l18;

import java.util.ArrayList;
import java.util.List;

/** An undirected weighted graph: an edge list plus adjacency lists. */
public final class Graph {
    private final int n;
    private final List<Edge> edges = new ArrayList<>();
    private final List<List<Edge>> adj = new ArrayList<>();

    public Graph(int n) {
        if (n < 0) throw new IllegalArgumentException("negative vertex count");
        this.n = n;
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
    }

    public void addEdge(int u, int v, int weight) {
        if (u < 0 || u >= n || v < 0 || v >= n) throw new IllegalArgumentException("bad vertex");
        Edge e = new Edge(u, v, weight);
        edges.add(e);
        adj.get(u).add(e);
        if (v != u) adj.get(v).add(e);
    }

    public int vertexCount() { return n; }

    public List<Edge> edges() { return List.copyOf(edges); }

    public List<Edge> adjacent(int v) { return List.copyOf(adj.get(v)); }

    /** Vertex names A, B, C, ... for the slides. */
    public static String name(int v) {
        return v < 26 ? String.valueOf((char) ('A' + v)) : "v" + v;
    }

    public static long total(List<Edge> tree) {
        long sum = 0;
        for (Edge e : tree) sum += e.weight();
        return sum;
    }
}
