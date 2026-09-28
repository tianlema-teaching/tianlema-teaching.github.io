package l07;

import java.util.NoSuchElementException;
import java.util.Objects;

/** A queue on a circular array that doubles when full. */
public class CircularArrayQueue<T> implements SimpleQueue<T> {
    private T[] slots;
    private int front; // index of the oldest element
    private int size;  // number of elements stored

    public CircularArrayQueue(int initialCapacity) {
        if (initialCapacity < 1) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        slots = newArray(initialCapacity);
    }

    @SuppressWarnings("unchecked") // the array only ever holds T values
    private static <T> T[] newArray(int capacity) {
        return (T[]) new Object[capacity];
    }

    @Override
    public void enqueue(T value) {
        Objects.requireNonNull(value, "null is reserved for empty slots and for poll on empty");
        if (size == slots.length) {
            grow();
        }
        int back = (front + size) % slots.length;
        slots[back] = value;
        size++;
    }

    @Override
    public T dequeue() {
        if (size == 0) {
            throw new NoSuchElementException("queue is empty");
        }
        T value = slots[front];
        slots[front] = null;
        front = (front + 1) % slots.length;
        size--;
        return value;
    }

    /** Like dequeue, but returns null instead of throwing when empty. */
    public T poll() {
        return size == 0 ? null : dequeue();
    }

    @Override
    public T peek() {
        if (size == 0) {
            throw new NoSuchElementException("queue is empty");
        }
        return slots[front];
    }

    private void grow() {
        T[] bigger = newArray(2 * slots.length);
        for (int i = 0; i < size; i++) {
            bigger[i] = slots[(front + i) % slots.length];
        }
        slots = bigger;
        front = 0;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public int size() {
        return size;
    }

    /** Every slot in index order, "_" for unused, then front and size. */
    public String slotsView() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < slots.length; i++) {
            sb.append(slots[i] == null ? "_" : String.valueOf(slots[i]));
            sb.append(i + 1 < slots.length ? ", " : "]");
        }
        return sb.append(" front=").append(front).append(" size=").append(size).toString();
    }
}
