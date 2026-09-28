package l15;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

/** Connected components of an undirected graph by repeated BFS: O(V + E). */
public final class Components {
    private Components() { }

    /** comp[v] is the component number of v, numbered 0, 1, ... in order of discovery. */
    public static int[] label(Graph g) {
        int n = g.vertexCount();
        int[] comp = new int[n];
        Arrays.fill(comp, -1);            // -1 means not yet reached
        int next = 0;
        for (int s = 0; s < n; s++) {
            if (comp[s] != -1) continue;
            Queue<Integer> queue = new ArrayDeque<>();
            comp[s] = next;
            queue.add(s);
            while (!queue.isEmpty()) {
                int u = queue.remove();
                for (int v : g.neighbors(u)) {
                    if (comp[v] == -1) {
                        comp[v] = next;
                        queue.add(v);
                    }
                }
            }
            next++;
        }
        return comp;
    }
}
