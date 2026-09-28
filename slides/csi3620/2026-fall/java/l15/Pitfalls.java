package l15;

import java.util.ArrayDeque;
import java.util.Queue;

/** A BFS variant that marks vertices when dequeued, to count the extra queue traffic. */
public final class Pitfalls {
    private Pitfalls() { }

    /** Returns how many times vertices are enqueued when marking happens on dequeue. */
    public static int enqueuesMarkOnDequeue(Graph g, int s) {
        boolean[] visited = new boolean[g.vertexCount()];
        Queue<Integer> queue = new ArrayDeque<>();
        queue.add(s);
        int enqueues = 1;
        while (!queue.isEmpty()) {
            int u = queue.remove();
            if (visited[u]) continue;     // a duplicate copy
            visited[u] = true;            // marked late
            for (int v : g.neighbors(u)) {
                if (!visited[v]) {
                    queue.add(v);         // v may already be waiting in the queue
                    enqueues++;
                }
            }
        }
        return enqueues;
    }
}
