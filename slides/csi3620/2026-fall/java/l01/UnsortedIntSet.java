package l01;

/** Data structure 1: values in arrival order. */
public class UnsortedIntSet extends ArrayIntSet {
    @Override
    public boolean contains(int x) {
        for (int i = 0; i < size; i++) {
            if (data[i] == x) {
                return true;
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
        data[size++] = x;              // append at the end
        return true;
    }
}
