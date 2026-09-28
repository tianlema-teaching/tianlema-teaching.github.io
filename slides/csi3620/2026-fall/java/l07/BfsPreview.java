package l07;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/** Breadth-first visiting order on an adjacency-list graph. */
public final class BfsPreview {

    private BfsPreview() {
    }

    public static List<Integer> order(List<List<Integer>> adjacency, int start) {
        boolean[] seen = new boolean[adjacency.size()];
        List<Integer> visited = new ArrayList<>();
        Queue<Integer> queue = new ArrayDeque<>();
        seen[start] = true;
        queue.offer(start);
        while (!queue.isEmpty()) {
            int v = queue.poll();
            visited.add(v);
            for (int w : adjacency.get(v)) {
                if (!seen[w]) {
                    seen[w] = true; // mark when enqueued, not when visited
                    queue.offer(w);
                }
            }
            Trace.record(() -> "visit " + v + ", queue " + queue); // front first
        }
        return visited;
    }
}
