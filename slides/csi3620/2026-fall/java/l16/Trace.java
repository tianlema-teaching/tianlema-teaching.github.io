package l16;

import java.util.ArrayList;
import java.util.List;

/** Prints the step-by-step states used on the Lecture 16 slides. */
final class Trace {
    private Trace() {
    }

    public static void main(String[] args) {
        Digraph g = Examples.courses();
        List<String> t = new ArrayList<>();
        System.out.println("== Kahn");
        System.out.println(g.names(TopologicalSort.kahnOutput(g, t)));
        t.forEach(System.out::println);
        t.clear();
        System.out.println("== DFS");
        System.out.println(g.names(new DepthFirstOrder(g, t).order()));
        t.forEach(System.out::println);
        t.clear();
        Digraph c = Examples.coursesWithCycle();
        System.out.println("== three colors on cyclic");
        System.out.println(c.names(DirectedCycle.findCycle(c, t)));
        t.forEach(System.out::println);
        t.clear();
        System.out.println("== Kahn on cyclic");
        System.out.println(c.names(TopologicalSort.kahnOutput(c, t)));
        t.forEach(System.out::println);
        t.clear();
        Graph u = Examples.undirectedExample();
        System.out.println("== union-find " + UndirectedCycle.hasCycleUnionFind(u, t));
        t.forEach(System.out::println);
    }
}
