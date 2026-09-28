package l14;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A binary min-heap. Keys live in an array list with 0-based indices:
 * the parent of i is (i - 1) / 2, its children are 2i + 1 and 2i + 2.
 * Invariant (heap order): every key is at least its parent's key.
 */
public final class MinHeap<T extends Comparable<? super T>> {
    private final List<T> a = new ArrayList<>();

    static int parent(int i) { return (i - 1) / 2; }
    static int left(int i) { return 2 * i + 1; }
    static int right(int i) { return 2 * i + 2; }

    public int size() { return a.size(); }

    public boolean isEmpty() { return a.isEmpty(); }

    /** The smallest key, without removing it. O(1). */
    public T peek() {
        if (a.isEmpty()) throw new NoSuchElementException("empty heap");
        return a.get(0);
    }

    /** Adds x as the new last leaf, then sifts it up. O(log n). */
    public void insert(T x) {
        Objects.requireNonNull(x, "heap keys must not be null");
        a.add(x);
        siftUp(a.size() - 1);
    }

    /** Removes and returns the smallest key. O(log n). */
    public T extractMin() {
        T min = peek();                   // throws if empty
        T last = a.remove(a.size() - 1);  // detach the last leaf
        if (!a.isEmpty()) {
            a.set(0, last);               // it replaces the root
            siftDown(0);
        }
        return min;
    }

    /** Builds a heap from the items in O(n): sift down every internal node, last first. */
    public static <T extends Comparable<? super T>> MinHeap<T> buildHeap(List<T> items) {
        MinHeap<T> h = new MinHeap<>();
        for (T x : items) h.a.add(Objects.requireNonNull(x, "heap keys must not be null"));
        for (int i = h.a.size() / 2 - 1; i >= 0; i--) {
            h.siftDown(i);
        }
        return h;
    }

    private void siftUp(int i) {
        while (i > 0 && less(i, parent(i))) {
            swap(i, parent(i));
            i = parent(i);
        }
    }

    private void siftDown(int i) {
        int n = a.size();
        while (left(i) < n) {
            int c = left(i);                  // pick the smaller child
            if (right(i) < n && less(right(i), c)) c = right(i);
            if (!less(c, i)) return;          // heap order holds here
            swap(i, c);
            i = c;
        }
    }

    private boolean less(int i, int j) { return a.get(i).compareTo(a.get(j)) < 0; }

    private void swap(int i, int j) {
        T t = a.get(i);
        a.set(i, a.get(j));
        a.set(j, t);
    }

    /** A copy of the backing array in index order (for tests and traces). */
    public List<T> toList() { return new ArrayList<>(a); }

    /** Checks heap order at every non-root index. */
    boolean isHeap() {
        for (int i = 1; i < a.size(); i++) {
            if (less(i, parent(i))) return false;
        }
        return true;
    }
}
