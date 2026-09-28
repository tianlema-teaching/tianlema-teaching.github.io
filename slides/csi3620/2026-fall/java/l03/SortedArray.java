package l03;

/** Insert into the sorted prefix arr[0..size-1]; finding is fast, shifting is not. */
public final class SortedArray {
    private SortedArray() { }

    /** Returns the index where value landed. Requires size < arr.length. */
    public static int insert(int[] arr, int size, int value) {
        if (size < 0 || size >= arr.length) {
            throw new IllegalArgumentException("no free slot: size " + size);
        }
        int low = 0;
        int high = size;
        while (low < high) {                // O(log n): find the spot
            int mid = low + (high - low) / 2;
            if (arr[mid] < value) {
                low = mid + 1;
            } else {
                high = mid;
            }
        }
        for (int i = size; i > low; i--) {  // O(n): shift the tail right
            arr[i] = arr[i - 1];
        }
        arr[low] = value;
        return low;
    }
}
