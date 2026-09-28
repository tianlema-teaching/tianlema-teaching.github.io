package l17;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/** Concrete checks for every class in l17. Prints "l17 OK" when all pass. */
public final class Checks {
    private Checks() {
    }

    static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    public static void main(String[] args) {
        runningExample();
        negativeEdge();
        negativeCycle();
        floydWarshall();
        edgeCases();
        randomAgreement();
        System.out.println("l17 OK");
    }

    static long[] dists(ShortestPaths sp, int n) {
        long[] d = new long[n];
        for (int v = 0; v < n; v++) {
            d[v] = sp.distTo(v);
        }
        return d;
    }

    static void runningExample() {
        WeightedDigraph g = Examples.running();
        ShortestPaths dj = Dijkstra.run(g, Examples.S);
        check(Arrays.equals(dists(dj, 6), new long[] {0, 3, 1, 4, 7, 8}), "Dijkstra distances");
        check(dj.pathTo(Examples.E).equals(List.of(0, 2, 1, 3, 4, 5)), "path S B A C D E");
        check(dj.pathTo(Examples.S).equals(List.of(0)), "path to source");
        ShortestPaths bf = BellmanFord.run(g, Examples.S).orElseThrow();
        check(Arrays.equals(dists(bf, 6), dists(dj, 6)), "Bellman-Ford agrees");
        check(Arrays.equals(FloydWarshall.run(g)[0], dists(dj, 6)), "Floyd-Warshall row S agrees");
        check(Arrays.equals(dists(Bfs.edgeCounts(g, 0), 6), new long[] {0, 1, 1, 2, 2, 3}), "BFS edge counts");
        check(Bfs.edgeCounts(g, 0).pathTo(Examples.E).equals(List.of(0, 1, 3, 5)), "BFS path S A C E");
        check(Arrays.equals(dists(brute(g, 0), 6), dists(dj, 6)), "brute force agrees");
    }

    static void negativeEdge() {
        WeightedDigraph g = Examples.negativeEdge();
        ShortestPaths dj = Dijkstra.run(g, 0);
        ShortestPaths bf = BellmanFord.run(g, 0).orElseThrow();
        check(Arrays.equals(dists(bf, 4), new long[] {0, 1, 5, 2}), "Bellman-Ford correct");
        check(Arrays.equals(dists(brute(g, 0), 4), dists(bf, 4)), "brute force agrees with Bellman-Ford");
        check(dj.distTo(3) == 3, "Dijkstra reports 3 for C");
        check(dj.distTo(3) != bf.distTo(3), "Dijkstra is wrong on C");
        check(dj.distTo(1) == 1, "A's value was lowered late, after A was finalized");
        check(bf.pathTo(3).equals(List.of(0, 2, 1, 3)), "true path S B A C");
        check(!FloydWarshall.hasNegativeCycle(FloydWarshall.run(g)), "no negative cycle");
    }

    static void negativeCycle() {
        WeightedDigraph g = Examples.negativeCycle();
        check(BellmanFord.run(g, 0).isEmpty(), "Bellman-Ford reports the negative cycle");
        check(FloydWarshall.hasNegativeCycle(FloydWarshall.run(g)), "Floyd-Warshall diagonal");
        long[][] d = FloydWarshall.run(g);
        check(d[0][0] == 0 && d[1][1] < 0 && d[2][2] < 0 && d[3][3] < 0, "only cycle vertices go negative");
        // A negative cycle that the source cannot reach does not stop Bellman-Ford.
        WeightedDigraph apart = new WeightedDigraph("S", "X", "Y");
        apart.addEdge(1, 2, -3);
        apart.addEdge(2, 1, 1);
        Optional<ShortestPaths> r = BellmanFord.run(apart, 0);
        check(r.isPresent() && !r.get().hasPathTo(1), "unreachable negative cycle is not reported");
        check(FloydWarshall.hasNegativeCycle(FloydWarshall.run(apart)), "Floyd-Warshall still sees it");
        // A negative diagonal entry does not locate a cycle: P lies only on the cycle P -> X -> P of
        // weight 2, yet the closed walk P -> X -> Y -> X -> P weighs -2, so d[P][P] goes negative.
        WeightedDigraph walk = new WeightedDigraph("P", "X", "Y");
        walk.addEdge(0, 1, 1);
        walk.addEdge(1, 0, 1);
        walk.addEdge(1, 2, -5);
        walk.addEdge(2, 1, 1);
        long[][] dw = FloydWarshall.run(walk);
        check(!negativeCycleFrom(walk, 0, 0, 0, new boolean[3]), "P is on no simple negative cycle");
        check(negativeCycleFrom(walk, 1, 1, 0, new boolean[3]), "X is on the negative cycle X -> Y -> X");
        check(dw[0][0] < 0 && dw[1][1] < 0 && dw[2][2] < 0, "yet every diagonal entry is negative");
    }

    static void floydWarshall() {
        WeightedDigraph g = Examples.fourVertex();
        long[][] d = FloydWarshall.run(g);
        long[][] expected = {{0, -1, 1, 1}, {5, 0, 6, 2}, {3, -2, 0, 0}, {3, 2, 4, 0}};
        check(Arrays.deepEquals(d, expected), "Floyd-Warshall matrix");
        for (int s = 0; s < 4; s++) {
            check(Arrays.equals(d[s], dists(BellmanFord.run(g, s).orElseThrow(), 4)), "row matches Bellman-Ford");
        }
        // Chain 0 -> 2 -> 3 -> 1: the path 0 to 1 needs two intermediates.
        WeightedDigraph chain = new WeightedDigraph("0", "1", "2", "3");
        chain.addEdge(0, 2, 1);
        chain.addEdge(2, 3, 1);
        chain.addEdge(3, 1, 1);
        check(FloydWarshall.run(chain)[0][1] == 3, "k outermost finds 0 -> 1");
        check(Pitfalls.floydWarshallKInnermost(chain)[0][1] == ShortestPaths.INF, "k innermost misses it");
    }

    static void edgeCases() {
        WeightedDigraph one = new WeightedDigraph("S");
        check(Dijkstra.run(one, 0).distTo(0) == 0, "single vertex");
        WeightedDigraph two = new WeightedDigraph("S", "T");
        ShortestPaths sp = Dijkstra.run(two, 0);
        check(!sp.hasPathTo(1) && sp.pathTo(1).isEmpty(), "unreachable vertex");
        check(!BellmanFord.run(two, 0).orElseThrow().hasPathTo(1), "unreachable in Bellman-Ford");
        check(FloydWarshall.run(two)[0][1] == ShortestPaths.INF, "unreachable in Floyd-Warshall");
        WeightedDigraph parallel = new WeightedDigraph("S", "T");
        parallel.addEdge(0, 1, 7);
        parallel.addEdge(0, 1, 2);
        check(Dijkstra.run(parallel, 0).distTo(1) == 2, "parallel edges: lighter one wins");
        check(FloydWarshall.run(parallel)[0][1] == 2, "parallel edges in the matrix");
        WeightedDigraph zero = new WeightedDigraph("S", "A", "B");
        zero.addEdge(0, 1, 0);
        zero.addEdge(1, 2, 0);
        check(Dijkstra.run(zero, 0).distTo(2) == 0, "zero weights are allowed");
    }

    /** Brute force: the lightest simple path to every vertex. Valid when no negative cycle is reachable. */
    static ShortestPaths brute(WeightedDigraph g, int source) {
        long[] best = new long[g.vertexCount()];
        Arrays.fill(best, ShortestPaths.INF);
        boolean[] onPath = new boolean[g.vertexCount()];
        explore(g, source, 0, onPath, best);
        return new ShortestPaths(best, new int[g.vertexCount()]);
    }

    private static void explore(WeightedDigraph g, int u, long length, boolean[] onPath, long[] best) {
        best[u] = Math.min(best[u], length);
        onPath[u] = true;
        for (WeightedDigraph.Edge e : g.edgesFrom(u)) {
            if (!onPath[e.to()]) {
                explore(g, e.to(), length + e.weight(), onPath, best);
            }
        }
        onPath[u] = false;
    }

    /** Brute force: is there a simple cycle with negative weight through a vertex reachable from source? */
    static boolean reachableNegativeCycle(WeightedDigraph g, int source) {
        ShortestPaths reach = Bfs.edgeCounts(g, source);
        for (int start = 0; start < g.vertexCount(); start++) {
            if (reach.hasPathTo(start) && negativeCycleFrom(g, start, start, 0, new boolean[g.vertexCount()])) {
                return true;
            }
        }
        return false;
    }

    private static boolean negativeCycleFrom(WeightedDigraph g, int start, int u, long length, boolean[] onPath) {
        onPath[u] = true;
        for (WeightedDigraph.Edge e : g.edgesFrom(u)) {
            if (e.to() == start && length + e.weight() < 0) {
                return true;
            }
            if (!onPath[e.to()] && negativeCycleFrom(g, start, e.to(), length + e.weight(), onPath)) {
                return true;
            }
        }
        onPath[u] = false;
        return false;
    }

    static void randomAgreement() {
        Random rnd = new Random(17);
        for (int trial = 0; trial < 3000; trial++) {
            int n = 1 + rnd.nextInt(6);
            boolean allowNegative = trial % 2 == 1;
            WeightedDigraph g = new WeightedDigraph(new String[n]);
            int m = rnd.nextInt(3 * n + 1);
            for (int i = 0; i < m; i++) {
                int w = allowNegative ? rnd.nextInt(13) - 3 : rnd.nextInt(10);
                g.addEdge(rnd.nextInt(n), rnd.nextInt(n), w);
            }
            long[][] fw = FloydWarshall.run(g);
            boolean anyNegativeCycle = false;
            for (int s = 0; s < n; s++) {
                Optional<ShortestPaths> bf = BellmanFord.run(g, s);
                boolean negative = reachableNegativeCycle(g, s);
                anyNegativeCycle |= negative;
                check(bf.isEmpty() == negative, "Bellman-Ford negative-cycle test");
                if (bf.isPresent()) {
                    long[] expected = dists(brute(g, s), n);
                    check(Arrays.equals(dists(bf.get(), n), expected), "Bellman-Ford distances");
                    check(Arrays.equals(fw[s], expected), "Floyd-Warshall row");
                    for (int v = 0; v < n; v++) {
                        List<Integer> path = bf.get().pathTo(v);
                        check(path.isEmpty() == (expected[v] == ShortestPaths.INF), "path exists");
                        if (!path.isEmpty()) {
                            check(path.get(0) == s && path.get(path.size() - 1) == v, "path ends");
                            check(pathWeight(g, path) == expected[v], "path weight equals distance");
                        }
                    }
                    if (!allowNegative) {
                        ShortestPaths dj = Dijkstra.run(g, s);
                        check(Arrays.equals(dists(dj, n), expected), "Dijkstra distances");
                        for (int v = 0; v < n; v++) {
                            if (dj.hasPathTo(v)) {
                                check(pathWeight(g, dj.pathTo(v)) == expected[v], "Dijkstra path weight");
                            }
                        }
                    }
                }
            }
            check(FloydWarshall.hasNegativeCycle(fw) == anyNegativeCycle, "Floyd-Warshall diagonal test");
            for (int v = 0; v < n; v++) {
                if (negativeCycleFrom(g, v, v, 0, new boolean[n])) {
                    check(fw[v][v] < 0, "every vertex on a simple negative cycle has a negative diagonal");
                }
            }
        }
    }

    /** Weight of a path, using the lightest edge between each consecutive pair. */
    static long pathWeight(WeightedDigraph g, List<Integer> path) {
        long total = 0;
        for (int i = 0; i + 1 < path.size(); i++) {
            long lightest = ShortestPaths.INF;
            for (WeightedDigraph.Edge e : g.edgesFrom(path.get(i))) {
                if (e.to() == path.get(i + 1)) {
                    lightest = Math.min(lightest, e.weight());
                }
            }
            check(lightest != ShortestPaths.INF, "path uses a real edge");
            total += lightest;
        }
        return total;
    }
}
