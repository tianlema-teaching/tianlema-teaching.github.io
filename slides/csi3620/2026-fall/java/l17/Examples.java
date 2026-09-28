package l17;

/** The graphs used on the Lecture 17 slides. */
final class Examples {
    static final int S = 0, A = 1, B = 2, C = 3, D = 4, E = 5;

    private Examples() {
    }

    /** The running example: non-negative weights, source S. */
    static WeightedDigraph running() {
        WeightedDigraph g = new WeightedDigraph("S", "A", "B", "C", "D", "E");
        g.addEdge(S, A, 4);
        g.addEdge(S, B, 1);
        g.addEdge(B, A, 2);
        g.addEdge(A, C, 1);
        g.addEdge(B, C, 5);
        g.addEdge(B, D, 8);
        g.addEdge(C, D, 3);
        g.addEdge(C, E, 6);
        g.addEdge(D, E, 1);
        return g;
    }

    /** One negative edge B -> A, no negative cycle. Vertices S=0, A=1, B=2, C=3. Edge A -> C is added first. */
    static WeightedDigraph negativeEdge() {
        WeightedDigraph g = new WeightedDigraph("S", "A", "B", "C");
        g.addEdge(1, 3, 1);
        g.addEdge(0, 1, 2);
        g.addEdge(0, 2, 5);
        g.addEdge(2, 1, -4);
        return g;
    }

    /** negativeEdge plus C -> B with weight 2: the cycle B -> A -> C -> B has weight -1. */
    static WeightedDigraph negativeCycle() {
        WeightedDigraph g = negativeEdge();
        g.addEdge(3, 2, 2);
        return g;
    }

    /** The four-vertex Floyd-Warshall example; one negative edge 2 -> 1, no negative cycle. */
    static WeightedDigraph fourVertex() {
        WeightedDigraph g = new WeightedDigraph("0", "1", "2", "3");
        g.addEdge(0, 1, 4);
        g.addEdge(0, 2, 1);
        g.addEdge(2, 1, -2);
        g.addEdge(1, 3, 2);
        g.addEdge(2, 3, 5);
        g.addEdge(3, 0, 3);
        return g;
    }
}
