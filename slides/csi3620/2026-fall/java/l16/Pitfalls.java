package l16;

import java.util.Arrays;

/** Deliberately wrong cycle tests, kept so Checks can show exactly how they fail. */
final class Pitfalls {
    private Pitfalls() {
    }

    /** WRONG for directed graphs: treats every edge to an already visited vertex as a cycle. */
    static boolean visitedMeansCycle(Digraph g) {
        boolean[] visited = new boolean[g.vertexCount()];
        for (int s = 0; s < g.vertexCount(); s++) {
            if (!visited[s] && visit(g, s, visited)) {
                return true;
            }
        }
        return false;
    }

    private static boolean visit(Digraph g, int u, boolean[] visited) {
        visited[u] = true;
        for (int v : g.neighbors(u)) {
            if (visited[v]) {
                return true;
            }
            if (visit(g, v, visited)) {
                return true;
            }
        }
        return false;
    }

    /** WRONG: searches only from vertex 0, so parts it cannot reach are never examined. */
    static boolean searchFromZeroOnly(Digraph g) {
        DirectedCycle.Color[] color = new DirectedCycle.Color[g.vertexCount()];
        Arrays.fill(color, DirectedCycle.Color.WHITE);
        return grayHit(g, 0, color);
    }

    private static boolean grayHit(Digraph g, int u, DirectedCycle.Color[] color) {
        color[u] = DirectedCycle.Color.GRAY;
        for (int v : g.neighbors(u)) {
            if (color[v] == DirectedCycle.Color.GRAY) {
                return true;
            }
            if (color[v] == DirectedCycle.Color.WHITE && grayHit(g, v, color)) {
                return true;
            }
        }
        color[u] = DirectedCycle.Color.BLACK;
        return false;
    }
}
