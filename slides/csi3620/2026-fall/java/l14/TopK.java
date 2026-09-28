package l14;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

/** The k largest values of a stream, kept in a size-k min-heap: O(n log k) time, O(k) space. */
public final class TopK {
    private TopK() { }

    public static List<Integer> largest(Iterable<Integer> values, int k) {
        if (k < 0) throw new IllegalArgumentException("k must be >= 0");
        PriorityQueue<Integer> heap = new PriorityQueue<>();   // min-heap
        for (int x : values) {
            if (k == 0) break;
            if (heap.size() < k) {
                heap.offer(x);
            } else if (x > heap.peek()) {                      // beats the weakest kept
                heap.poll();
                heap.offer(x);
            }
        }
        List<Integer> out = new ArrayList<>(heap);
        out.sort(Collections.reverseOrder());                  // largest first
        return out;
    }
}
