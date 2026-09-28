package l18;

import java.util.ArrayList;
import java.util.List;

/** Kruskal's algorithm: scan edges by weight, keep those that join two trees. */
public final class Kruskal {
    private Kruskal() { }

    public static List<Edge> mst(Graph g) {
        return mst(g, new ArrayList<>());
    }

    /** Returns a minimum spanning forest; log receives one line per edge examined. */
    public static List<Edge> mst(Graph g, List<String> log) {
        List<Edge> sorted = new ArrayList<>(g.edges());
        sorted.sort(null);                   // natural order: by weight
        UnionFind sets = new UnionFind(g.vertexCount());
        List<Edge> tree = new ArrayList<>();
        for (Edge e : sorted) {
            if (tree.size() == g.vertexCount() - 1) break;   // tree complete
            if (sets.union(e.u(), e.v())) {
                tree.add(e);
                log.add("accept " + e);
            } else {
                log.add("reject " + e + " (cycle)");
            }
        }
        return tree;
    }
}
