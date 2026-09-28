package l16;

import java.util.Arrays;
import java.util.List;

/** Cycle detection in an undirected graph, by DFS and by union-find; also correct on multigraphs. */
public final class UndirectedCycle {
    private UndirectedCycle() {
    }

    public static boolean hasCycleDfs(Graph g) {
        boolean[] visited = new boolean[g.vertexCount()];
        for (int s = 0; s < g.vertexCount(); s++) {
            if (!visited[s] && dfs(g, s, -1, visited)) {
                return true;
            }
        }
        return false;
    }

    private static boolean dfs(Graph g, int u, int parent, boolean[] visited) {
        visited[u] = true;
        for (int v : g.neighbors(u)) {
            if (!visited[v]) {
                if (dfs(g, v, u, visited)) {
                    return true;
                }
            } else if (v != parent) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasCycleUnionFind(Graph g) {
        return hasCycleUnionFind(g, null);
    }

    public static boolean hasCycleUnionFind(Graph g, List<String> trace) {
        int[] root = new int[g.vertexCount()];
        for (int v = 0; v < root.length; v++) {
            root[v] = v;
        }
        for (int[] e : g.edges()) {
            int a = find(root, e[0]);
            int b = find(root, e[1]);
            if (trace != null) {
                trace.add("edge " + e[0] + "-" + e[1] + " roots " + a + "," + b);
            }
            if (a == b) {
                return true;
            }
            root[a] = b;
            if (trace != null) {
                trace.add("root=" + Arrays.toString(root));
            }
        }
        return false;
    }

    private static int find(int[] root, int v) {
        while (root[v] != v) {
            v = root[v];
        }
        return v;
    }
}
