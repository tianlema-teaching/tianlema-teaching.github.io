package l18;

/** The six-vertex graph traced on every step-by-step slide. */
public final class RunningExample {
    private RunningExample() { }

    public static final int A = 0, B = 1, C = 2, D = 3, E = 4, F = 5;

    public static Graph graph() {
        Graph g = new Graph(6);
        g.addEdge(A, B, 4);
        g.addEdge(A, C, 2);
        g.addEdge(B, C, 5);
        g.addEdge(B, D, 10);
        g.addEdge(C, E, 3);
        g.addEdge(C, D, 8);
        g.addEdge(D, E, 7);
        g.addEdge(D, F, 6);
        g.addEdge(E, F, 9);
        return g;
    }
}
