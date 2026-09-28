package l07;

/** The queue ADT: first in, first out. */
public interface SimpleQueue<T> {
    void enqueue(T value); // add at the back
    T dequeue();           // remove and return the front; throws if empty
    T peek();              // return the front, keep it; throws if empty
    boolean isEmpty();
    int size();
}
