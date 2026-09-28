package l17;

import java.util.ArrayList;
import java.util.List;

/** Prints the step-by-step states used on the Lecture 17 slides. */
final class Trace {
    private Trace() {
    }

    public static void main(String[] args) {
        List<String> t = new ArrayList<>();
        WeightedDigraph g = Examples.running();
        ShortestPaths sp = Dijkstra.run(g, Examples.S, t);
        System.out.println("== Dijkstra running");
        t.forEach(System.out::println);
        System.out.println("path to E " + sp.pathTo(Examples.E));
        System.out.println("bfs " + BellmanFord.show(dists(Bfs.edgeCounts(g, 0), 6)));
        t.clear();
        WeightedDigraph neg = Examples.negativeEdge();
        System.out.println("== Dijkstra negative");
        Dijkstra.run(neg, 0, t);
        t.forEach(System.out::println);
        t.clear();
        System.out.println("== Bellman-Ford negative");
        BellmanFord.run(neg, 0, t).ifPresent(r -> System.out.println("dist " + BellmanFord.show(dists(r, 4))));
        t.forEach(System.out::println);
        t.clear();
        System.out.println("== Bellman-Ford negative cycle " + BellmanFord.run(Examples.negativeCycle(), 0, t).isPresent());
        t.forEach(System.out::println);
        t.clear();
        System.out.println("== Floyd-Warshall");
        FloydWarshall.run(Examples.fourVertex(), t);
        t.forEach(System.out::println);
        long[][] nc = FloydWarshall.run(Examples.negativeCycle());
        System.out.println("negcycle diag " + nc[0][0] + " " + nc[1][1] + " " + nc[2][2] + " " + nc[3][3]);
    }

    static long[] dists(ShortestPaths sp, int n) {
        long[] d = new long[n];
        for (int v = 0; v < n; v++) {
            d[v] = sp.distTo(v);
        }
        return d;
    }
}
