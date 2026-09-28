package l16;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/** Concrete checks for every class in l16. Prints "l16 OK" when all pass. */
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
        cycleExample();
        undirected();
        pitfalls();
        randomAgreement();
        System.out.println("l16 OK");
    }

    static void runningExample() {
        Digraph g = Examples.courses();
        check(g.vertexCount() == 7 && g.edgeCount() == 8, "size");
        Optional<List<Integer>> kahn = TopologicalSort.kahn(g);
        check(kahn.isPresent(), "DAG has an order");
        check(g.names(kahn.get()).equals(List.of("Intro", "Data", "Logic", "Sys", "Algo", "Net", "Cap")), "Kahn order");
        List<Integer> dfs = TopologicalSort.dfsOrder(g);
        check(g.names(dfs).equals(List.of("Intro", "Logic", "Data", "Sys", "Net", "Algo", "Cap")), "DFS order");
        check(TopologicalSort.isTopologicalOrder(g, kahn.get()), "Kahn valid");
        check(TopologicalSort.isTopologicalOrder(g, dfs), "DFS valid");
        check(!kahn.get().equals(dfs), "two different valid orders");
        check(!TopologicalSort.isTopologicalOrder(g, List.of(1, 0, 2, 3, 4, 5, 6)), "Data before Intro is invalid");
        check(DirectedCycle.findCycle(g).isEmpty(), "no cycle in DAG");
        check(countOrders(g) == 9, "number of topological orders of the course DAG");
    }

    /** Counts topological orders by brute force over all permutations. */
    static int countOrders(Digraph g) {
        int[] count = {0};
        permute(g, new ArrayList<>(), new boolean[g.vertexCount()], count);
        return count[0];
    }

    private static void permute(Digraph g, List<Integer> prefix, boolean[] used, int[] count) {
        if (prefix.size() == g.vertexCount()) {
            if (TopologicalSort.isTopologicalOrder(g, prefix)) {
                count[0]++;
            }
            return;
        }
        for (int v = 0; v < g.vertexCount(); v++) {
            if (!used[v]) {
                used[v] = true;
                prefix.add(v);
                permute(g, prefix, used, count);
                prefix.remove(prefix.size() - 1);
                used[v] = false;
            }
        }
    }

    static void cycleExample() {
        Digraph c = Examples.coursesWithCycle();
        List<Integer> cycle = DirectedCycle.findCycle(c);
        check(c.names(cycle).equals(List.of("Data", "Sys", "Net")), "reported cycle");
        check(DirectedCycle.isCycle(c, cycle), "reported cycle uses real edges");
        check(TopologicalSort.kahn(c).isEmpty(), "Kahn reports a cycle");
        check(c.names(TopologicalSort.kahnOutput(c, null)).equals(List.of("Intro", "Logic")), "Kahn stalls after two");
        Digraph loop = new Digraph("X");
        loop.addEdge(0, 0);
        check(DirectedCycle.findCycle(loop).equals(List.of(0)), "self-loop is a cycle");
        Digraph empty = new Digraph();
        check(TopologicalSort.kahn(empty).orElseThrow().isEmpty(), "empty graph");
        check(TopologicalSort.dfsOrder(empty).isEmpty(), "empty graph DFS");
    }

    static void undirected() {
        Graph tree = new Graph(5);
        tree.addEdge(0, 1);
        tree.addEdge(0, 2);
        tree.addEdge(2, 3);
        tree.addEdge(2, 4);
        check(!UndirectedCycle.hasCycleDfs(tree) && !UndirectedCycle.hasCycleUnionFind(tree), "tree has no cycle");
        Graph withCycle = Examples.undirectedExample();
        check(UndirectedCycle.hasCycleDfs(withCycle) && UndirectedCycle.hasCycleUnionFind(withCycle), "triangle found");
        Graph split = new Graph(6);
        split.addEdge(0, 1);
        split.addEdge(2, 3);
        split.addEdge(3, 4);
        split.addEdge(4, 2);
        check(UndirectedCycle.hasCycleDfs(split), "cycle in a component not containing vertex 0");
        boolean rejected = false;
        try {
            split.addEdge(1, 0);
        } catch (IllegalArgumentException e) {
            rejected = true;
        }
        check(rejected, "parallel edge rejected");
        multigraphs();
    }

    /** The parent check also works on multigraphs: parallel edges and self-loops are reported. */
    static void multigraphs() {
        // u = 0 and w = 1 joined by two separate edges. The child w skips both copies (both lead to
        // its parent u); u's scan of the second copy finds w visited and not u's parent.
        Graph pair = Graph.multigraph(2);
        pair.addEdge(0, 1);
        pair.addEdge(0, 1);
        check(pair.neighbors(0).equals(List.of(1, 1)) && pair.neighbors(1).equals(List.of(0, 0)), "two copies stored");
        check(UndirectedCycle.hasCycleDfs(pair), "DFS reports the 2-cycle of parallel edges");
        check(UndirectedCycle.hasCycleUnionFind(pair), "union-find reports the 2-cycle");
        Graph single = Graph.multigraph(2);
        single.addEdge(0, 1);
        check(!UndirectedCycle.hasCycleDfs(single), "one copy is not a cycle");
        Graph loop = Graph.multigraph(2);
        loop.addEdge(0, 1);
        loop.addEdge(1, 1);
        check(UndirectedCycle.hasCycleDfs(loop), "DFS reports a self-loop at a non-root vertex");
        check(UndirectedCycle.hasCycleUnionFind(loop), "union-find reports the self-loop");
        Random rnd = new Random(1616);
        for (int trial = 0; trial < 3000; trial++) {
            int n = 1 + rnd.nextInt(6);
            Graph g = Graph.multigraph(n);
            int m = rnd.nextInt(2 * n);
            for (int i = 0; i < m; i++) {
                g.addEdge(rnd.nextInt(n), rnd.nextInt(n));
            }
            boolean forest = g.edges().size() == n - components(g);
            check(UndirectedCycle.hasCycleDfs(g) == !forest, "multigraph: DFS matches edge count test");
            check(UndirectedCycle.hasCycleUnionFind(g) == !forest, "multigraph: union-find matches edge count test");
        }
    }

    static void pitfalls() {
        // A DAG with two routes to C: A -> B -> C and A -> C.
        Digraph diamond = new Digraph("A", "B", "C");
        diamond.addEdge(0, 1);
        diamond.addEdge(1, 2);
        diamond.addEdge(0, 2);
        check(DirectedCycle.findCycle(diamond).isEmpty(), "diamond is a DAG");
        check(Pitfalls.visitedMeansCycle(diamond), "visited-only test gives a false cycle");
        Graph undirectedView = new Graph(3);
        undirectedView.addEdge(0, 1);
        undirectedView.addEdge(1, 2);
        undirectedView.addEdge(0, 2);
        check(UndirectedCycle.hasCycleDfs(undirectedView), "undirected view has a cycle");
        // Vertex 0 is isolated; the cycle 1 -> 2 -> 1 is unreachable from it.
        Digraph apart = new Digraph("P", "Q", "R");
        apart.addEdge(1, 2);
        apart.addEdge(2, 1);
        check(!Pitfalls.searchFromZeroOnly(apart), "one start misses the cycle");
        check(!DirectedCycle.findCycle(apart).isEmpty(), "outer loop finds it");
    }

    static void randomAgreement() {
        Random rnd = new Random(16);
        for (int trial = 0; trial < 3000; trial++) {
            int n = 1 + rnd.nextInt(7);
            Digraph g = new Digraph(new String[n]);
            int m = rnd.nextInt(2 * n + 1);
            for (int i = 0; i < m; i++) {
                g.addEdge(rnd.nextInt(n), rnd.nextInt(n));
            }
            Optional<List<Integer>> kahn = TopologicalSort.kahn(g);
            List<Integer> cycle = DirectedCycle.findCycle(g);
            check(kahn.isPresent() == cycle.isEmpty(), "Kahn and colors agree");
            if (kahn.isPresent()) {
                check(TopologicalSort.isTopologicalOrder(g, kahn.get()), "Kahn valid");
                check(TopologicalSort.isTopologicalOrder(g, TopologicalSort.dfsOrder(g)), "DFS valid");
                check(countOrders(g) > 0, "DAG has an order");
            } else {
                check(DirectedCycle.isCycle(g, cycle), "cycle is real");
                check(countOrders(g) == 0, "cyclic graph has no order");
            }
        }
        for (int trial = 0; trial < 3000; trial++) {
            int n = 1 + rnd.nextInt(8);
            Graph g = new Graph(n);
            int attempts = rnd.nextInt(2 * n);
            for (int i = 0; i < attempts; i++) {
                int u = rnd.nextInt(n);
                int v = rnd.nextInt(n);
                if (u != v && !g.neighbors(u).contains(v)) {
                    g.addEdge(u, v);
                }
            }
            boolean forest = g.edges().size() == n - components(g);
            check(UndirectedCycle.hasCycleDfs(g) == !forest, "DFS matches edge count test");
            check(UndirectedCycle.hasCycleUnionFind(g) == !forest, "union-find matches edge count test");
        }
    }

    private static int components(Graph g) {
        boolean[] seen = new boolean[g.vertexCount()];
        int count = 0;
        for (int s = 0; s < g.vertexCount(); s++) {
            if (!seen[s]) {
                count++;
                List<Integer> stack = new ArrayList<>(List.of(s));
                seen[s] = true;
                while (!stack.isEmpty()) {
                    int u = stack.remove(stack.size() - 1);
                    for (int v : g.neighbors(u)) {
                        if (!seen[v]) {
                            seen[v] = true;
                            stack.add(v);
                        }
                    }
                }
            }
        }
        return count;
    }
}
