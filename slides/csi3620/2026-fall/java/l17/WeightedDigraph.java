package l17;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A weighted directed graph on vertices 0..n-1 with adjacency lists and a global edge list. */
public final class WeightedDigraph {
    /** The directed edge from -> to with an integer weight (which may be negative). */
    public record Edge(int from, int to, int weight) {
    }

    private final String[] names;
    private final List<List<Edge>> adj = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();

    public WeightedDigraph(String... names) {
        this.names = names.clone();
        for (int v = 0; v < names.length; v++) {
            adj.add(new ArrayList<>());
        }
    }

    public void addEdge(int from, int to, int weight) {
        if (from < 0 || from >= names.length || to < 0 || to >= names.length) {
            throw new IndexOutOfBoundsException("bad edge " + from + " -> " + to);
        }
        Edge e = new Edge(from, to, weight);
        adj.get(from).add(e);
        edges.add(e);
    }

    public int vertexCount() {
        return names.length;
    }

    /** Edges leaving u, in insertion order. */
    public List<Edge> edgesFrom(int u) {
        return Collections.unmodifiableList(adj.get(u));
    }

    /** All edges, in insertion order. */
    public List<Edge> edges() {
        return Collections.unmodifiableList(edges);
    }

    public String name(int v) {
        return names[v];
    }
}
