package l16;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A directed graph on vertices 0..n-1, stored as adjacency lists. Each vertex has a display name. */
public final class Digraph {
    private final String[] names;
    private final List<List<Integer>> adj = new ArrayList<>();
    private int edgeCount;

    public Digraph(String... names) {
        this.names = names.clone();
        for (int v = 0; v < names.length; v++) {
            adj.add(new ArrayList<>());
        }
    }

    /** Adds the directed edge u -> v. Neighbors are kept in insertion order. */
    public void addEdge(int u, int v) {
        check(u);
        check(v);
        adj.get(u).add(v);
        edgeCount++;
    }

    public int vertexCount() {
        return names.length;
    }

    public int edgeCount() {
        return edgeCount;
    }

    public List<Integer> neighbors(int u) {
        check(u);
        return Collections.unmodifiableList(adj.get(u));
    }

    public String name(int v) {
        check(v);
        return names[v];
    }

    public List<String> names(List<Integer> vertices) {
        List<String> out = new ArrayList<>();
        for (int v : vertices) {
            out.add(name(v));
        }
        return out;
    }

    private void check(int v) {
        if (v < 0 || v >= names.length) {
            throw new IndexOutOfBoundsException("no vertex " + v);
        }
    }
}
