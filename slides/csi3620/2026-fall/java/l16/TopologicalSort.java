package l16;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

/** Two ways to order a DAG so that every edge u -> v puts u before v. */
public final class TopologicalSort {
    private TopologicalSort() {
    }

    /** Kahn's algorithm. Returns an order of all vertices, or empty if the graph has a cycle. */
    public static Optional<List<Integer>> kahn(Digraph g) {
        List<Integer> order = kahnOutput(g, null);
        return order.size() == g.vertexCount()
                ? Optional.of(order) : Optional.empty();
    }

    /** Kahn's algorithm, returning whatever it output; fewer than V vertices means a cycle. */
    public static List<Integer> kahnOutput(Digraph g, List<String> trace) {
        int n = g.vertexCount();
        int[] inDegree = new int[n];
        for (int u = 0; u < n; u++) {
            for (int v : g.neighbors(u)) {
                inDegree[v]++;
            }
        }
        Deque<Integer> queue = new ArrayDeque<>();
        for (int v = 0; v < n; v++) {
            if (inDegree[v] == 0) {
                queue.add(v);
            }
        }
        List<Integer> order = new ArrayList<>();
        if (trace != null) {
            trace.add("start indeg=" + Arrays.toString(inDegree) + " queue=" + g.names(new ArrayList<>(queue)));
        }
        while (!queue.isEmpty()) {
            int u = queue.remove();
            order.add(u);
            for (int v : g.neighbors(u)) {
                inDegree[v]--;
                if (inDegree[v] == 0) {
                    queue.add(v);
                }
            }
            if (trace != null) {
                trace.add("out " + g.name(u) + " indeg=" + Arrays.toString(inDegree)
                        + " queue=" + g.names(new ArrayList<>(queue)) + " order=" + g.names(order));
            }
        }
        return order;
    }

    /** DFS-based order: reverse of finish order. Assumes g is a DAG (check with DirectedCycle first). */
    public static List<Integer> dfsOrder(Digraph g) {
        return new DepthFirstOrder(g, null).order();
    }

    /** True when every edge u -> v has u before v in order, and order lists each vertex once. */
    public static boolean isTopologicalOrder(Digraph g, List<Integer> order) {
        int n = g.vertexCount();
        if (order.size() != n) {
            return false;
        }
        int[] position = new int[n];
        Arrays.fill(position, -1);
        for (int i = 0; i < n; i++) {
            int v = order.get(i);
            if (v < 0 || v >= n || position[v] != -1) {
                return false;
            }
            position[v] = i;
        }
        for (int u = 0; u < n; u++) {
            for (int v : g.neighbors(u)) {
                if (position[u] >= position[v]) {
                    return false;
                }
            }
        }
        return true;
    }
}
