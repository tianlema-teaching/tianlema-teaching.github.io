package l18;

/** An undirected weighted edge between vertices u and v. */
public record Edge(int u, int v, int weight) implements Comparable<Edge> {

    /** The endpoint that is not x. */
    public int other(int x) {
        if (x == u) return v;
        if (x == v) return u;
        throw new IllegalArgumentException("vertex " + x + " is not on " + this);
    }

    /** Order by weight; ties by endpoints so every run is repeatable. */
    @Override
    public int compareTo(Edge that) {
        if (weight != that.weight) return Integer.compare(weight, that.weight);
        if (u != that.u) return Integer.compare(u, that.u);
        return Integer.compare(v, that.v);
    }

    @Override
    public String toString() {
        return Graph.name(u) + "-" + Graph.name(v) + " " + weight;
    }
}
