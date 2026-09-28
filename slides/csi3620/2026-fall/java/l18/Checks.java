package l18;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/** Concrete checks for every class in l18; prints "l18 OK". */
public final class Checks {
    private Checks() { }

    static void check(boolean ok, String what) {
        if (!ok) throw new AssertionError(what);
    }

    static void eq(Object expected, Object actual, String what) {
        if (!expected.equals(actual)) {
            throw new AssertionError(what + ": expected " + expected + " but got " + actual);
        }
    }

    public static void main(String[] args) {
        edgeChecks();
        unionFindChecks();
        runningExample();
        edgeCases();
        randomGraphs();
        System.out.println("l18 OK");
    }

    static void edgeChecks() {
        Edge e = new Edge(0, 2, 5);
        eq(2, e.other(0), "other end");
        eq(0, e.other(2), "other end back");
        eq("A-C 5", e.toString(), "edge name");
        boolean threw = false;
        try { e.other(1); } catch (IllegalArgumentException ex) { threw = true; }
        check(threw, "other() rejects a vertex not on the edge");
        check(new Edge(0, 1, 3).compareTo(new Edge(0, 2, 4)) < 0, "lighter edge first");
    }

    static void unionFindChecks() {
        UnionFind uf = new UnionFind(6);
        eq(6, uf.setCount(), "six singletons");
        eq("A B C D E F", parents(uf), "parents at start");
        check(uf.union(0, 2), "A,C merge");
        eq("A B A D E F", parents(uf), "parents after union A,C");
        check(uf.union(2, 4), "C,E merge");
        eq("A B A D A F", parents(uf), "parents after union C,E");
        check(uf.union(0, 1), "A,B merge");
        eq("A A A D A F", parents(uf), "parents after union A,B");
        check(!uf.union(1, 2), "B,C already joined");
        eq("A A A D A F", parents(uf), "parents after refused B,C");
        check(uf.union(3, 5), "D,F merge");
        eq("A A A D A D", parents(uf), "parents after union D,F");
        eq(2, uf.setCount(), "two sets before the last union");
        check(uf.connected(1, 4) && !uf.connected(1, 3), "membership");
        check(uf.union(3, 4), "D,E merge");
        eq("D A A D A D", parents(uf), "parents after union D,E");
        eq(1, uf.setCount(), "one set");
        // Build a two-level chain, then compress on find.
        UnionFind chain = new UnionFind(5);
        chain.union(0, 1);                   // root 0, rank 1
        chain.union(2, 3);                   // root 2, rank 1
        chain.union(0, 2);                   // root 0, rank 2; 3 -> 2 -> 0
        eq(2, chain.parentOf(3), "3 hangs under 2 before find");
        eq(0, chain.find(3), "root of 3");
        eq(0, chain.parentOf(3), "path compressed: 3 points at root");
        chain.union(4, 0);                   // rank 0 under rank 2
        eq(0, chain.parentOf(4), "lower rank goes under");
    }

    static void runningExample() {
        Graph g = RunningExample.graph();
        List<String> klog = new ArrayList<>();
        List<Edge> k = Kruskal.mst(g, klog);
        eq(List.of("accept A-C 2", "accept C-E 3", "accept A-B 4", "reject B-C 5 (cycle)",
                "accept D-F 6", "accept D-E 7"), klog, "Kruskal trace");
        eq(22L, Graph.total(k), "Kruskal total");
        List<String> plog = new ArrayList<>();
        List<Edge> p = LazyPrim.mst(g, RunningExample.A, plog);
        eq(List.of("start A; queue [A-C 2, A-B 4]",
                "add A-C 2, reach C", "queue [C-E 3, A-B 4, B-C 5, C-D 8]",
                "add C-E 3, reach E", "queue [A-B 4, B-C 5, D-E 7, C-D 8, E-F 9]",
                "add A-B 4, reach B", "queue [B-C 5, D-E 7, C-D 8, E-F 9, B-D 10]",
                "skip B-C 5 (both ends in tree)",
                "add D-E 7, reach D", "queue [D-F 6, C-D 8, E-F 9, B-D 10]",
                "add D-F 6, reach F", "queue [C-D 8, E-F 9, B-D 10]"), plog, "Prim trace");
        eq(22L, Graph.total(p), "Prim total");
        eq(new HashSet<>(k), new HashSet<>(p), "same tree (distinct weights)");
        for (int s = 0; s < g.vertexCount(); s++) {
            eq(new HashSet<>(k), new HashSet<>(LazyPrim.mst(g, s)), "Prim from " + Graph.name(s));
        }
        eq(22L, bruteForce(g), "brute force agrees");
        eq(1, countMinimum(g), "unique MST");
        // Shortest-path tree from A is a different tree.
        List<Edge> spt = ShortestPathTree.from(g, RunningExample.A);
        eq(Set.of(new Edge(0, 1, 4), new Edge(0, 2, 2), new Edge(2, 3, 8), new Edge(2, 4, 3),
                new Edge(4, 5, 9)), new HashSet<>(spt), "shortest-path tree from A");
        eq(26L, Graph.total(spt), "SPT weight");
        check(!new HashSet<>(k).equals(new HashSet<>(spt)), "SPT differs from MST");
        eq(12L, pathLength(k, 0, 3), "A to D inside the MST");
        eq(10L, pathLength(spt, 0, 3), "A to D inside the SPT");
    }

    static void edgeCases() {
        Graph one = new Graph(1);
        check(Kruskal.mst(one).isEmpty() && LazyPrim.mst(one, 0).isEmpty(), "one vertex");
        Graph split = new Graph(4);           // {A,B} and {C,D}
        split.addEdge(0, 1, 3);
        split.addEdge(2, 3, 1);
        eq(2, Kruskal.mst(split).size(), "Kruskal gives a forest");
        eq(1, LazyPrim.mst(split, 0).size(), "Prim spans only its start's component");
        Graph multi = new Graph(2);          // parallel edges and a self-loop
        multi.addEdge(0, 1, 5);
        multi.addEdge(0, 1, 2);
        multi.addEdge(1, 1, 1);
        eq(2L, Graph.total(Kruskal.mst(multi)), "Kruskal keeps the lighter parallel edge");
        eq(2L, Graph.total(LazyPrim.mst(multi, 0)), "Prim keeps the lighter parallel edge");
        Graph tie = new Graph(3);            // equal weights: three different MSTs
        tie.addEdge(0, 1, 1);
        tie.addEdge(1, 2, 1);
        tie.addEdge(0, 2, 1);
        eq(3, countMinimum(tie), "three minimum trees on an equal-weight triangle");
    }

    static void randomGraphs() {
        Random rnd = new Random(18);
        for (int trial = 0; trial < 3000; trial++) {
            int n = 1 + rnd.nextInt(7);
            boolean distinct = rnd.nextBoolean();
            Graph g = randomConnected(n, rnd, distinct);
            long k = Graph.total(Kruskal.mst(g));
            eq(n - 1, Kruskal.mst(g).size(), "Kruskal has V-1 edges");
            int start = rnd.nextInt(n);
            List<Edge> pt = LazyPrim.mst(g, start);
            eq(n - 1, pt.size(), "Prim has V-1 edges");
            eq(k, Graph.total(pt), "Kruskal and Prim totals");
            if (g.edges().size() <= 14) {
                eq(k, bruteForce(g), "brute force total");
                if (distinct) {
                    eq(1, countMinimum(g), "distinct weights give one MST");
                    eq(new HashSet<>(Kruskal.mst(g)), new HashSet<>(pt), "same edges");
                }
            }
        }
        // Larger graphs: the two algorithms still agree.
        for (int trial = 0; trial < 200; trial++) {
            Graph g = randomConnected(50 + rnd.nextInt(50), rnd, false);
            eq(Graph.total(Kruskal.mst(g)), Graph.total(LazyPrim.mst(g, 0)), "large totals");
        }
    }

    /** The stored parents of elements 0..5 as vertex names, e.g. "A B A D E F". */
    static String parents(UnionFind uf) {
        StringBuilder sb = new StringBuilder();
        for (int x = 0; x < 6; x++) {
            if (x > 0) sb.append(' ');
            sb.append(Graph.name(uf.parentOf(x)));
        }
        return sb.toString();
    }

    /** A random spanning tree plus random extra edges; distinct or repeated weights. */
    static Graph randomConnected(int n, Random rnd, boolean distinct) {
        List<int[]> pairs = new ArrayList<>();
        for (int v = 1; v < n; v++) pairs.add(new int[] {rnd.nextInt(v), v});
        int extra = rnd.nextInt(n + 3);
        for (int i = 0; i < extra && n > 1; i++) {
            int a = rnd.nextInt(n), b = rnd.nextInt(n);
            if (a != b) pairs.add(new int[] {a, b});
        }
        List<Integer> weights = new ArrayList<>();
        for (int i = 0; i < pairs.size(); i++) weights.add(distinct ? i + 1 : 1 + rnd.nextInt(4));
        java.util.Collections.shuffle(weights, rnd);
        Graph g = new Graph(n);
        for (int i = 0; i < pairs.size(); i++) g.addEdge(pairs.get(i)[0], pairs.get(i)[1], weights.get(i));
        return g;
    }

    /** Minimum weight over every (V-1)-edge subset that is a spanning tree. */
    static long bruteForce(Graph g) {
        long best = Long.MAX_VALUE;
        for (List<Edge> t : spanningTrees(g)) best = Math.min(best, Graph.total(t));
        return best;
    }

    static int countMinimum(Graph g) {
        long best = bruteForce(g);
        int count = 0;
        for (List<Edge> t : spanningTrees(g)) if (Graph.total(t) == best) count++;
        return count;
    }

    static List<List<Edge>> spanningTrees(Graph g) {
        List<Edge> es = g.edges();
        int n = g.vertexCount();
        List<List<Edge>> out = new ArrayList<>();
        for (int mask = 0; mask < (1 << es.size()); mask++) {
            if (Integer.bitCount(mask) != n - 1) continue;
            UnionFind uf = new UnionFind(n);
            List<Edge> t = new ArrayList<>();
            boolean acyclic = true;
            for (int i = 0; i < es.size() && acyclic; i++) {
                if ((mask & (1 << i)) == 0) continue;
                acyclic = uf.union(es.get(i).u(), es.get(i).v());
                t.add(es.get(i));
            }
            if (acyclic) out.add(t);          // V-1 edges, no cycle: spanning tree
        }
        return out;
    }

    /** Length of the unique path from s to t inside a tree. */
    static long pathLength(List<Edge> tree, int s, int t) {
        return walk(tree, s, -1, t);
    }

    private static long walk(List<Edge> tree, int at, int from, int goal) {
        if (at == goal) return 0;
        for (Edge e : tree) {
            if (e.u() != at && e.v() != at) continue;
            int next = e.other(at);
            if (next == from) continue;
            long rest = walk(tree, next, at, goal);
            if (rest >= 0) return rest + e.weight();
        }
        return -1;
    }
}
