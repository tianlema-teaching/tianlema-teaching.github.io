package l01;

import java.util.Arrays;

/** Shared storage for the two array-backed sets: an array plus a count. */
public abstract class ArrayIntSet implements IntSet {
    protected int[] data = new int[4];
    protected int size = 0;

    @Override
    public int size() {
        return size;
    }

    protected void ensureRoom() {
        if (size == data.length) {
            data = Arrays.copyOf(data, 2 * data.length);
        }
    }

    public int[] toArray() {
        return Arrays.copyOf(data, size);
    }
}
