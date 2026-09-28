package l14;

/** In-place heapsort into ascending order using a max-heap (as in CLRS Chapter 6). */
public final class Heapsort {
    private Heapsort() { }

    public static <T extends Comparable<? super T>> void sort(T[] a) {
        int n = a.length;
        for (int i = n / 2 - 1; i >= 0; i--) {
            siftDown(a, i, n);            // build a max-heap in O(n)
        }
        for (int end = n - 1; end > 0; end--) {
            swap(a, 0, end);              // largest key to its final slot
            siftDown(a, 0, end);          // restore the heap on a[0..end-1]
        }
    }

    /** Sifts a[i] down in the max-heap a[0..n-1]. */
    private static <T extends Comparable<? super T>> void siftDown(T[] a, int i, int n) {
        while (2 * i + 1 < n) {
            int c = 2 * i + 1;                               // pick the larger child
            if (c + 1 < n && a[c + 1].compareTo(a[c]) > 0) c++;
            if (a[c].compareTo(a[i]) <= 0) return;           // max-heap order holds
            swap(a, i, c);
            i = c;
        }
    }

    private static <T> void swap(T[] a, int i, int j) {
        T t = a[i];
        a[i] = a[j];
        a[j] = t;
    }
}
