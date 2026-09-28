package l16;

/** The running example of Lecture 16: a made-up course-prerequisite DAG. */
final class Examples {
    static final int INTRO = 0, DATA = 1, LOGIC = 2, ALGO = 3, SYS = 4, NET = 5, CAP = 6;

    private Examples() {
    }

    /** Edge u -> v means "take u before v". */
    static Digraph courses() {
        Digraph g = new Digraph("Intro", "Data", "Logic", "Algo", "Sys", "Net", "Cap");
        g.addEdge(INTRO, DATA);
        g.addEdge(INTRO, LOGIC);
        g.addEdge(DATA, ALGO);
        g.addEdge(DATA, SYS);
        g.addEdge(LOGIC, ALGO);
        g.addEdge(ALGO, CAP);
        g.addEdge(SYS, NET);
        g.addEdge(NET, CAP);
        return g;
    }

    /** The same courses plus a mistaken rule Net -> Data, which creates the cycle Data -> Sys -> Net -> Data. */
    static Digraph coursesWithCycle() {
        Digraph g = courses();
        g.addEdge(NET, DATA);
        return g;
    }

    /** A small undirected example: edges 0-1, 1-2, 3-4, then 2-0 closes the triangle 0-1-2. */
    static Graph undirectedExample() {
        Graph g = new Graph(5);
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(3, 4);
        g.addEdge(2, 0);
        return g;
    }
}
