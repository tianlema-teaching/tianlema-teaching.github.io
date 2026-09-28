package l16;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/** Runs DFS from every unvisited vertex and records vertices in reverse finish order. */
final class DepthFirstOrder {
    private final Digraph g;
    private final boolean[] visited;
    private final Deque<Integer> finished = new ArrayDeque<>();
    private final List<String> trace; // null unless the slides' trace is wanted

    DepthFirstOrder(Digraph g, List<String> trace) {
        this.g = g;
        this.trace = trace;
        visited = new boolean[g.vertexCount()];
        for (int s = 0; s < g.vertexCount(); s++) {
            if (!visited[s]) {
                visit(s);
            }
        }
    }

    private void visit(int u) {
        visited[u] = true;
        if (trace != null) {
            trace.add("enter " + g.name(u));
        }
        for (int v : g.neighbors(u)) {
            if (!visited[v]) {
                visit(v);
            }
        }
        finished.push(u); // push adds at the front: latest finisher first
        if (trace != null) {
            trace.add("finish " + g.name(u) + " list=" + g.names(new ArrayList<>(finished)));
        }
    }

    /** Vertices in reverse finish order: a topological order when g is a DAG. */
    List<Integer> order() {
        return new ArrayList<>(finished);
    }
}
