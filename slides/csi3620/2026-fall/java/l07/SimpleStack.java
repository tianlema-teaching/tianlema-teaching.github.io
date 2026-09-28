package l07;

/** The stack ADT: last in, first out. */
public interface SimpleStack<T> {
    void push(T value);   // add on top
    T pop();              // remove and return the top; throws if empty
    T peek();             // return the top without removing; throws if empty
    boolean isEmpty();
    int size();
}
