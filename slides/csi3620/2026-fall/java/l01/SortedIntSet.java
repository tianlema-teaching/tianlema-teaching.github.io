package l01;

/** Data structure 2: values kept in increasing order. */
public class SortedIntSet extends ArrayIntSet {
    @Override
    public boolean contains(int x) {
        int low = 0, high = size - 1;
        while (low <= high) {
            int mid = low + (high - low) / 2;
            if (data[mid] == x) {
                return true;
            } else if (data[mid] < x) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return false;
    }

    @Override
    public boolean add(int x) {
        if (contains(x)) {
            return false;
        }
        ensureRoom();
        int i = size - 1;
        while (i >= 0 && data[i] > x) {
            data[i + 1] = data[i];     // shift a bigger value right
            i--;
        }
        data[i + 1] = x;
        size++;
        return true;
    }
}
