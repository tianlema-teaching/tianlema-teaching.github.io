package l18;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/** Lazy Prim: grow one tree from start; the queue may hold stale edges. */
public final class LazyPrim {
    private LazyPrim() { }

    public static List<Edge> mst(Graph g, int start) {
        return mst(g, start, new ArrayList<>());
    }

    /** Returns a minimum spanning tree of start's component. */
    public static List<Edge> mst(Graph g, int start, List<String> log) {
        boolean[] inTree = new boolean[g.vertexCount()];
        PriorityQueue<Edge> pq = new PriorityQueue<>();
        List<Edge> tree = new ArrayList<>();
        visit(g, start, inTree, pq);
        log.add("start " + Graph.name(start) + "; queue " + sorted(pq));
        while (!pq.isEmpty() && tree.size() < g.vertexCount() - 1) {
            Edge e = pq.poll();              // lightest edge in the queue
            if (inTree[e.u()] && inTree[e.v()]) {
                log.add("skip " + e + " (both ends in tree)");
                continue;                    // stale: no longer crossing
            }
            int next = inTree[e.u()] ? e.v() : e.u();
            tree.add(e);
            log.add("add " + e + ", reach " + Graph.name(next));
            visit(g, next, inTree, pq);
            log.add("queue " + sorted(pq));
        }
        return tree;
    }

    /** The queue's contents in poll order, for the trace. */
    static List<Edge> sorted(PriorityQueue<Edge> pq) {
        List<Edge> copy = new ArrayList<>(pq);
        copy.sort(null);
        return copy;
    }

    private static void visit(Graph g, int v, boolean[] inTree,
            PriorityQueue<Edge> pq) {
        inTree[v] = true;
        for (Edge e : g.adjacent(v)) {
            if (!inTree[e.other(v)]) pq.add(e);
        }
    }
}
