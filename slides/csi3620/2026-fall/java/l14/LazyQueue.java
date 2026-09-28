package l14;

import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * java.util.PriorityQueue has no decreaseKey. The lazy workaround: insert a
 * new entry with the smaller key and skip stale entries when they are polled.
 * Items are 0..n-1; smaller keys come out first.
 */
public final class LazyQueue {
    private record Entry(int item, int key) { }

    private final PriorityQueue<Entry> pq = new PriorityQueue<>(Comparator.comparingInt(Entry::key));
    private final int[] best;       // current key of each item
    private final boolean[] done;   // item already polled
    private final boolean[] seen;   // item offered at least once
    private int staleSkipped;

    public LazyQueue(int n) {
        best = new int[n];
        Arrays.fill(best, Integer.MAX_VALUE);
        done = new boolean[n];
        seen = new boolean[n];
    }

    /** Inserts the item or lowers its key; a key that is not smaller is ignored. */
    public void offerOrDecrease(int item, int key) {
        if (done[item] || (seen[item] && key >= best[item])) return;
        seen[item] = true;
        best[item] = key;
        pq.offer(new Entry(item, key));   // any older entry is now stale
    }

    /** Removes the item with the smallest current key, or returns -1 if none is left. */
    public int pollItem() {
        while (!pq.isEmpty()) {
            Entry e = pq.poll();
            if (done[e.item()] || e.key() != best[e.item()]) {
                staleSkipped++;           // stale: a newer entry replaced it
                continue;
            }
            done[e.item()] = true;
            return e.item();
        }
        return -1;
    }

    public int key(int item) { return best[item]; }

    public int staleSkipped() { return staleSkipped; }
}
