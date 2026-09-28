package l02;

/** Insertion sort that also reports how many key comparisons it made. */
public final class InsertionSort {
    private InsertionSort() { }

    public static long sort(int[] a) {
        long comparisons = 0;
        for (int i = 1; i < a.length; i++) {
            int key = a[i];
            int j = i - 1;
            while (j >= 0) {
                comparisons++;
                if (a[j] <= key) {
                    break;
                }
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = key;
        }
        return comparisons;
    }
}
