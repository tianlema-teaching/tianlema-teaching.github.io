package l18;

/** Disjoint sets over 0..n-1 with union by rank and path compression. */
public final class UnionFind {
    private final int[] parent;
    private final int[] rank;
    private int sets;

    public UnionFind(int n) {
        parent = new int[n];
        rank = new int[n];
        sets = n;
        for (int i = 0; i < n; i++) parent[i] = i;
    }

    /** The representative (root) of x's set; compresses the path. */
    public int find(int x) {
        if (parent[x] != x) parent[x] = find(parent[x]);
        return parent[x];
    }

    /** Merges the sets of a and b; false if they were already one set. */
    public boolean union(int a, int b) {
        int ra = find(a), rb = find(b);
        if (ra == rb) return false;
        if (rank[ra] < rank[rb]) { int t = ra; ra = rb; rb = t; }
        parent[rb] = ra;                     // lower rank goes under
        if (rank[ra] == rank[rb]) rank[ra]++;
        sets--;
        return true;
    }

    public boolean connected(int a, int b) { return find(a) == find(b); }

    public int setCount() { return sets; }

    /** For tests: the stored parent, without compressing. */
    int parentOf(int x) { return parent[x]; }
}
