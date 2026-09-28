package l12;

import java.util.ArrayList;
import java.util.List;

/**
 * A hash map that resolves collisions by separate chaining: each slot of the table holds a
 * singly linked list of entries whose keys hash to that slot. Null keys are rejected.
 */
public class ChainingHashMap<K, V> {

    private static final class Node<K, V> {
        final K key;
        V value;
        Node<K, V> next;

        Node(K key, V value, Node<K, V> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    private Node<K, V>[] table;
    private int size;
    private final double maxLoad;

    public ChainingHashMap() {
        this(16, 0.75);
    }

    public ChainingHashMap(int capacity, double maxLoad) {
        if (capacity < 1 || maxLoad <= 0) {
            throw new IllegalArgumentException("capacity >= 1 and maxLoad > 0 required");
        }
        this.table = newTable(capacity);
        this.maxLoad = maxLoad;
    }

    @SuppressWarnings("unchecked") // Java cannot create a generic array directly
    private static <K, V> Node<K, V>[] newTable(int capacity) {
        return (Node<K, V>[]) new Node<?, ?>[capacity];
    }

    private int indexFor(Object key, int m) {
        return Math.floorMod(key.hashCode(), m);   // always in 0..m-1
    }

    public V get(Object key) {
        for (Node<K, V> e = table[indexFor(key, table.length)]; e != null; e = e.next) {
            if (e.key.equals(key)) {
                return e.value;
            }
        }
        return null;
    }

    public boolean containsKey(Object key) {
        for (Node<K, V> e = table[indexFor(key, table.length)]; e != null; e = e.next) {
            if (e.key.equals(key)) {
                return true;
            }
        }
        return false;
    }

    /** Returns the previous value for key, or null if the key was absent. */
    public V put(K key, V value) {
        if (key == null) {
            throw new NullPointerException("null keys are not supported");
        }
        int i = indexFor(key, table.length);
        for (Node<K, V> e = table[i]; e != null; e = e.next) {
            if (e.key.equals(key)) {          // key present: replace the value
                V old = e.value;
                e.value = value;
                return old;
            }
        }
        table[i] = new Node<>(key, value, table[i]);   // new key: insert at the head
        size++;
        if (size > maxLoad * table.length) {
            resize(2 * table.length);         // double: amortized O(1) per put
        }
        return null;
    }

    /** Returns the removed value, or null if the key was absent. */
    public V remove(Object key) {
        int i = indexFor(key, table.length);
        for (Node<K, V> prev = null, e = table[i]; e != null; prev = e, e = e.next) {
            if (e.key.equals(key)) {
                if (prev == null) {
                    table[i] = e.next;        // unlink the head
                } else {
                    prev.next = e.next;       // unlink from the middle
                }
                size--;
                return e.value;
            }
        }
        return null;
    }

    private void resize(int newCapacity) {
        Node<K, V>[] old = table;
        table = newTable(newCapacity);
        for (Node<K, V> head : old) {
            for (Node<K, V> e = head; e != null; e = e.next) {
                int i = indexFor(e.key, newCapacity);   // rehash every key
                table[i] = new Node<>(e.key, e.value, table[i]);
            }
        }
    }

    public int size() {
        return size;
    }

    public int capacity() {
        return table.length;
    }

    public List<K> keys() {
        List<K> out = new ArrayList<>();
        for (Node<K, V> head : table) {
            for (Node<K, V> e = head; e != null; e = e.next) {
                out.add(e.key);
            }
        }
        return out;
    }

    /** The table as text, one slot per line, e.g. "2: 23 -> 44". */
    public String bucketString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < table.length; i++) {
            sb.append(i).append(':');
            for (Node<K, V> e = table[i]; e != null; e = e.next) {
                sb.append(e == table[i] ? " " : " -> ").append(e.key);
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
