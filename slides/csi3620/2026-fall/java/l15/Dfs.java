package l15;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

/** Depth-first search over the whole graph, with CLRS-style discovery and finish times. */
public final class Dfs {
    public final int[] d;          // discovery time, 1..2V
    public final int[] f;          // finish time
    public final int[] parent;     // DFS forest parent; -1 for roots
    public final List<Integer> order = new ArrayList<>();   // discovery order
    private final Graph g;
    private int time;

    /** Runs DFS from every undiscovered vertex, in increasing vertex order. */
    public Dfs(Graph g) {
        this.g = g;
        int n = g.vertexCount();
        d = new int[n];
        f = new int[n];
        parent = new int[n];
        Arrays.fill(parent, -1);
        for (int s = 0; s < n; s++) {
            if (d[s] == 0) visit(s);      // d == 0 means undiscovered
        }
    }

    private void visit(int u) {
        d[u] = ++time;                    // discovered
        order.add(u);
        for (int v : g.neighbors(u)) {
            if (d[v] == 0) {
                parent[v] = u;
                visit(v);
            }
        }
        f[u] = ++time;                    // finished: all neighbors explored
    }

    /** Iterative DFS with an explicit stack: marks a vertex when it is popped. */
    public static List<Integer> iterativeOrder(Graph g, int s) {
        boolean[] visited = new boolean[g.vertexCount()];
        List<Integer> order = new ArrayList<>();
        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(s);
        while (!stack.isEmpty()) {
            int u = stack.pop();
            if (visited[u]) continue;     // a stale copy: already visited
            visited[u] = true;
            order.add(u);
            for (int v : g.neighbors(u)) {
                if (!visited[v]) stack.push(v);
            }
        }
        return order;
    }

    /** Same, but pushes neighbors in reverse so they pop in list order. */
    public static List<Integer> iterativeOrderReversed(Graph g, int s) {
        boolean[] visited = new boolean[g.vertexCount()];
        List<Integer> order = new ArrayList<>();
        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(s);
        while (!stack.isEmpty()) {
            int u = stack.pop();
            if (visited[u]) continue;
            visited[u] = true;
            order.add(u);
            List<Integer> nbrs = g.neighbors(u);
            for (int k = nbrs.size() - 1; k >= 0; k--) {
                if (!visited[nbrs.get(k)]) stack.push(nbrs.get(k));
            }
        }
        return order;
    }
}
