package l16;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

/** Cycle detection in a directed graph: DFS with three colors, reporting one cycle via parent pointers. */
public final class DirectedCycle {
    enum Color { WHITE, GRAY, BLACK }

    private final Digraph g;
    private final Color[] color;
    private final int[] parent;
    private final List<String> trace; // null unless the slides' trace is wanted
    private List<Integer> cycle = List.of();

    private DirectedCycle(Digraph g, List<String> trace) {
        this.g = g;
        this.trace = trace;
        int n = g.vertexCount();
        color = new Color[n];
        Arrays.fill(color, Color.WHITE);
        parent = new int[n];
        Arrays.fill(parent, -1);
        for (int s = 0; s < n && cycle.isEmpty(); s++) {
            if (color[s] == Color.WHITE) {
                dfs(s);
            }
        }
    }

    /** Returns the vertices of one directed cycle in edge order, or an empty list if g is a DAG. */
    public static List<Integer> findCycle(Digraph g) {
        return findCycle(g, null);
    }

    public static List<Integer> findCycle(Digraph g, List<String> trace) {
        return new DirectedCycle(g, trace).cycle;
    }

    private void dfs(int u) {
        color[u] = Color.GRAY;
        log("gray " + g.name(u));
        for (int v : g.neighbors(u)) {
            if (color[v] == Color.GRAY) {
                log("back edge " + g.name(u) + "->" + g.name(v));
                cycle = cycleFrom(u, v);
            } else if (color[v] == Color.WHITE) {
                parent[v] = u;
                dfs(v);
            }
            if (!cycle.isEmpty()) {
                return;
            }
        }
        color[u] = Color.BLACK;
        log("black " + g.name(u));
    }

    /** The back edge u -> v closes a cycle: walk parent pointers from u up to v. */
    private List<Integer> cycleFrom(int u, int v) {
        Deque<Integer> path = new ArrayDeque<>();
        for (int x = u; x != v; x = parent[x]) {
            path.push(x);
        }
        path.push(v);
        return new ArrayList<>(path);
    }

    private void log(String event) {
        if (trace != null) {
            trace.add(event);
        }
    }

    /** True when consecutive vertices (and last back to first) are joined by edges of g. */
    public static boolean isCycle(Digraph g, List<Integer> cycle) {
        if (cycle.isEmpty()) {
            return false;
        }
        for (int i = 0; i < cycle.size(); i++) {
            int from = cycle.get(i);
            int to = cycle.get((i + 1) % cycle.size());
            if (!g.neighbors(from).contains(to)) {
                return false;
            }
        }
        return true;
    }
}
