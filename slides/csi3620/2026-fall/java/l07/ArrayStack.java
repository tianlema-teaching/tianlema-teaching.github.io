package l07;

import java.util.Arrays;
import java.util.NoSuchElementException;

/** A stack on a resizing array; the top is items[size - 1]. */
public class ArrayStack<T> implements SimpleStack<T> {
    private T[] items;
    private int size;
    private long copies; // element copies made by resizing, for the analysis

    @SuppressWarnings("unchecked") // the array only ever holds T values
    public ArrayStack(int initialCapacity) {
        if (initialCapacity < 1) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        items = (T[]) new Object[initialCapacity];
    }

    public ArrayStack() {
        this(2);
    }

    @Override
    public void push(T value) {
        if (size == items.length) {
            resize(2 * items.length);
        }
        items[size++] = value;
    }

    @Override
    public T pop() {
        if (size == 0) {
            throw new NoSuchElementException("stack is empty");
        }
        T value = items[--size];
        items[size] = null; // drop the reference so it can be collected
        return value;
    }

    @Override
    public T peek() {
        if (size == 0) {
            throw new NoSuchElementException("stack is empty");
        }
        return items[size - 1];
    }

    private void resize(int capacity) {
        items = Arrays.copyOf(items, capacity);
        copies += size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public int size() {
        return size;
    }

    public int capacity() {
        return items.length;
    }

    public long copiesSoFar() {
        return copies;
    }

    /** Every slot, bottom first; "_" marks an unused slot. */
    public String slotsView() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < items.length; i++) {
            sb.append(i < size ? String.valueOf(items[i]) : "_");
            sb.append(i + 1 < items.length ? ", " : "]");
        }
        return sb.toString();
    }
}
