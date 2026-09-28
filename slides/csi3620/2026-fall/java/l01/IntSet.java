package l01;

/** The abstract data type: what a set of ints can do, not how it is stored. */
public interface IntSet {
    boolean add(int x);        // false if x was already present
    boolean contains(int x);
    int size();
}
