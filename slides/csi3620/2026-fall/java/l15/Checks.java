package l15;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public final class Checks {
    private Checks() { }

    static void check(boolean ok, String what) {
        if (!ok) throw new AssertionError(what);
    }

    /** The lecture's running example: 0..5 connected, plus the separate edge 6-7. */
    static Graph example() {
        Graph g = new Graph(8, false);
        int[][] edges = {{0, 1}, {0, 2}, {1, 3}, {2, 3}, {2, 4}, {3, 5}, {4, 5}, {6, 7}};
        for (int[] e : edges) g.addEdge(e[0], e[1]);
        return g;
    }

    public static void main(String[] args) {
        graphBasics();
        bfsExample();
        dfsExample();
        componentsExample();
        randomGraphs();
        deepPath();
        System.out.println("l15 OK");
    }

    static void graphBasics() {
        Graph g = example();
        check(g.vertexCount() == 8 && g.edgeCount() == 8, "counts");
        check(g.neighbors(2).equals(List.of(0, 3, 4)), "neighbors of 2");
        check(g.hasEdge(3, 1) && g.hasEdge(1, 3) && !g.hasEdge(0, 5), "hasEdge");
        check(g.degree(3) == 3 && g.degree(6) == 1, "degree");
        int sum = 0;
        for (int v = 0; v < 8; v++) sum += g.degree(v);
        check(sum == 2 * g.edgeCount(), "handshake: degrees sum to 2|E|");
        Graph d = new Graph(3, true);
        d.addEdge(0, 1);
        d.addEdge(1, 2);
        check(d.hasEdge(0, 1) && !d.hasEdge(1, 0) && d.degree(2) == 0, "directed edges");
        boolean threw = false;
        try { g.addEdge(0, 8); } catch (IndexOutOfBoundsException e) { threw = true; }
        check(threw, "bad vertex rejected");
        check(new Graph(0, false).vertexCount() == 0, "empty graph");
    }

    static void bfsExample() {
        Graph g = example();
        Bfs b = new Bfs(g, 0);
        check(b.order.equals(List.of(0, 1, 2, 3, 4, 5)), "BFS order " + b.order);
        check(Arrays.equals(b.dist, new int[] {0, 1, 1, 2, 2, 3, -1, -1}), "dist " + Arrays.toString(b.dist));
        check(Arrays.equals(b.parent, new int[] {-1, 0, 0, 1, 2, 3, -1, -1}), "parent " + Arrays.toString(b.parent));
        check(b.pathTo(5).equals(List.of(0, 1, 3, 5)), "path to 5");
        check(b.pathTo(0).equals(List.of(0)), "path to source");
        check(b.pathTo(7).isEmpty(), "unreachable");
        check(Pitfalls.enqueuesMarkOnDequeue(g, 0) == 8, "mark-on-dequeue enqueues 8 times");
    }

    static void dfsExample() {
        Graph g = example();
        Dfs t = new Dfs(g);
        check(t.order.equals(List.of(0, 1, 3, 2, 4, 5, 6, 7)), "DFS order " + t.order);
        check(Arrays.equals(t.d, new int[] {1, 2, 4, 3, 5, 6, 13, 14}), "d " + Arrays.toString(t.d));
        check(Arrays.equals(t.f, new int[] {12, 11, 9, 10, 8, 7, 16, 15}), "f " + Arrays.toString(t.f));
        check(Arrays.equals(t.parent, new int[] {-1, 0, 3, 1, 2, 4, -1, 6}), "parent " + Arrays.toString(t.parent));
        check(Dfs.iterativeOrder(g, 0).equals(List.of(0, 2, 4, 5, 3, 1)), "iterative " + Dfs.iterativeOrder(g, 0));
        check(Dfs.iterativeOrderReversed(g, 0).equals(List.of(0, 1, 3, 2, 4, 5)), "reversed push");
    }

    static void componentsExample() {
        int[] comp = Components.label(example());
        check(Arrays.equals(comp, new int[] {0, 0, 0, 0, 0, 0, 1, 1}), "components");
        Graph lone = new Graph(3, false);
        check(Arrays.equals(Components.label(lone), new int[] {0, 1, 2}), "isolated vertices");
    }

    /** Reference BFS distances by a plain level-by-level relaxation, independent of Bfs. */
    static int[] levels(Graph g, int s) {
        int n = g.vertexCount();
        int[] dist = new int[n];
        Arrays.fill(dist, -1);
        dist[s] = 0;
        for (int round = 0; round < n; round++) {
            for (int u = 0; u < n; u++) {
                if (dist[u] != round) continue;
                for (int v : g.neighbors(u)) if (dist[v] == -1) dist[v] = round + 1;
            }
        }
        return dist;
    }

    static void randomGraphs() {
        Random rnd = new Random(3620);
        for (int trial = 0; trial < 400; trial++) {
            int n = 1 + rnd.nextInt(12);
            boolean directed = rnd.nextBoolean();
            Graph g = new Graph(n, directed);
            int m = rnd.nextInt(2 * n + 1);
            for (int k = 0; k < m; k++) g.addEdge(rnd.nextInt(n), rnd.nextInt(n));
            Bfs b = new Bfs(g, 0);
            check(Arrays.equals(b.dist, levels(g, 0)), "BFS distances match reference");
            for (int v = 0; v < n; v++) {
                List<Integer> p = b.pathTo(v);
                if (b.dist[v] == -1) { check(p.isEmpty(), "no path"); continue; }
                check(p.size() == b.dist[v] + 1 && p.get(0) == 0 && p.get(p.size() - 1) == v, "path shape");
                for (int k = 0; k + 1 < p.size(); k++) check(g.hasEdge(p.get(k), p.get(k + 1)), "path uses edges");
            }
            Dfs t = new Dfs(g);
            for (int u = 0; u < n; u++) {
                check(1 <= t.d[u] && t.d[u] < t.f[u] && t.f[u] <= 2 * n, "times in range");
                for (int v : g.neighbors(u)) {
                    boolean nested = t.d[u] < t.d[v] && t.f[v] < t.f[u];
                    boolean ancestor = t.d[v] < t.d[u] && t.f[u] < t.f[v];
                    if (!directed) check(nested || ancestor || u == v, "undirected: no cross edges");
                    else check(t.d[v] < t.f[u], "directed: v discovered before u finishes");
                }
            }
            for (int u = 0; u < n; u++) {       // parenthesis theorem, all pairs
                for (int v = 0; v < n; v++) {
                    if (u == v) continue;
                    boolean disjoint = t.f[u] < t.d[v] || t.f[v] < t.d[u];
                    boolean uInsideV = t.d[v] < t.d[u] && t.f[u] < t.f[v];
                    boolean vInsideU = t.d[u] < t.d[v] && t.f[v] < t.f[u];
                    check(disjoint || uInsideV || vInsideU, "intervals nested or disjoint");
                    check(uInsideV == isDescendant(t.parent, u, v), "nested exactly for descendants");
                }
            }
            List<Integer> rec = t.order.subList(0, b.order.size());
            if (!directed) {
                check(rec.equals(Dfs.iterativeOrderReversed(g, 0)), "reversed-push iterative equals recursive");
                check(new HashSet<>(Dfs.iterativeOrder(g, 0)).equals(new HashSet<>(b.order)), "same reachable set");
                int[] comp = Components.label(g);
                for (int u = 0; u < n; u++) {
                    for (int v = 0; v < n; v++) {
                        boolean same = new Bfs(g, u).dist[v] != -1;
                        check(same == (comp[u] == comp[v]), "components match reachability");
                    }
                }
            } else {
                check(Dfs.iterativeOrderReversed(g, 0).equals(rec), "directed: reversed-push equals recursive");
            }
        }
    }

    /** True if u is a proper descendant of v in the DFS forest given by parent. */
    static boolean isDescendant(int[] parent, int u, int v) {
        for (int w = parent[u]; w != -1; w = parent[w]) {
            if (w == v) return true;
        }
        return false;
    }

    static void deepPath() {
        int n = 200_000;
        Graph path = new Graph(n, false);
        for (int v = 0; v + 1 < n; v++) path.addEdge(v, v + 1);
        check(Dfs.iterativeOrder(path, 0).size() == n, "iterative DFS handles a long path");
        check(new Bfs(path, 0).dist[n - 1] == n - 1, "BFS on a long path");
    }
}
