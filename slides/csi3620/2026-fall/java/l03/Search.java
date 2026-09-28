package l03;

/** Linear search, binary search (iterative and recursive), and lower bound. */
public final class Search {
    private Search() { }

    /** Index of target in arr, or -1. Works on any order. */
    public static int linearSearch(int[] arr, int target) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == target) {
                return i;
            }
        }
        return -1;
    }

    /** Index of target in arr, or -1. Requires arr sorted ascending. */
    public static int binarySearch(int[] arr, int target) {
        int low = 0;
        int high = arr.length - 1;
        while (low <= high) {               // if present: in arr[low..high]
            int mid = low + (high - low) / 2;
            if (arr[mid] == target) {
                return mid;
            } else if (arr[mid] < target) {
                low = mid + 1;              // arr[low..mid] are all too small
            } else {
                high = mid - 1;             // arr[mid..high] are all too big
            }
        }
        return -1;                          // range empty: not present
    }

    /** Same contract as binarySearch, written recursively. */
    public static int binarySearchRecursive(int[] arr, int target) {
        return search(arr, target, 0, arr.length - 1);
    }

    private static int search(int[] arr, int target, int low, int high) {
        if (low > high) {
            return -1;                      // base case: empty range
        }
        int mid = low + (high - low) / 2;
        if (arr[mid] == target) {
            return mid;
        } else if (arr[mid] < target) {
            return search(arr, target, mid + 1, high);
        } else {
            return search(arr, target, low, mid - 1);
        }
    }

    /** First index i with arr[i] >= target; arr.length if none. Requires sorted arr. */
    public static int lowerBound(int[] arr, int target) {
        int low = 0;
        int high = arr.length;          // undecided: [low, high); answer may be high
        while (low < high) {
            int mid = low + (high - low) / 2;
            if (arr[mid] < target) {
                low = mid + 1;          // mid and everything left of it are too small
            } else {
                high = mid;             // mid might be the answer: keep it
            }
        }
        return low;
    }
}
