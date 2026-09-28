package l12;

import java.util.ArrayList;
import java.util.List;

/**
 * A hash map with open addressing and linear probing. Removed slots hold a tombstone so that
 * later searches keep probing past them. Null keys are rejected.
 */
public class LinearProbingHashMap<K, V> {

    private static final Object TOMBSTONE = new Object();   // marks a removed slot

    private Object[] keys;      // null = never used, TOMBSTONE = removed, else a key of type K
    private Object[] vals;
    private int size;           // live keys
    private int tombstones;     // removed slots not yet cleaned up
    private final double maxLoad;

    public LinearProbingHashMap() {
        this(16, 0.5);
    }

    public LinearProbingHashMap(int capacity, double maxLoad) {
        if (capacity < 2 || maxLoad <= 0 || maxLoad >= 1) {
            throw new IllegalArgumentException("capacity >= 2 and 0 < maxLoad < 1 required");
        }
        keys = new Object[capacity];
        vals = new Object[capacity];
        this.maxLoad = maxLoad;
    }

    private int home(Object key) {
        return Math.floorMod(key.hashCode(), keys.length);
    }

    /** Slot holding key, or -1. Probes past tombstones; stops at a never-used slot. */
    private int find(Object key) {
        int m = keys.length;
        for (int j = 0, i = home(key); j < m; j++, i = (i + 1) % m) {
            if (keys[i] == null) {
                return -1;                    // a never-used slot ends the search
            }
            if (keys[i] != TOMBSTONE && keys[i].equals(key)) {
                return i;
            }
        }
        return -1;                            // probed every slot
    }

    @SuppressWarnings("unchecked") // vals holds only values of type V
    private V valueAt(int i) {
        return (V) vals[i];
    }

    public V get(Object key) {
        int i = find(key);
        return i < 0 ? null : valueAt(i);
    }

    public boolean containsKey(Object key) {
        return find(key) >= 0;
    }

    public V put(K key, V value) {
        if (key == null) {
            throw new NullPointerException("null keys are not supported");
        }
        int i = find(key);
        if (i >= 0) {                         // key present: replace the value
            V old = valueAt(i);
            vals[i] = value;
            return old;
        }
        if (size + tombstones + 1 > maxLoad * keys.length) {
            int newCapacity = keys.length;
            while (size + 1 > maxLoad * newCapacity / 2) {
                newCapacity *= 2;             // grow only if live keys need it
            }
            resize(newCapacity);              // also clears tombstones
        }
        int m = keys.length;
        i = home(key);
        while (keys[i] != null && keys[i] != TOMBSTONE) {
            i = (i + 1) % m;                  // first free slot: empty or tombstone
        }
        if (keys[i] == TOMBSTONE) {
            tombstones--;                     // reuse the removed slot
        }
        keys[i] = key;
        vals[i] = value;
        size++;
        return null;
    }

    public V remove(Object key) {
        int i = find(key);
        if (i < 0) {
            return null;
        }
        V old = valueAt(i);
        keys[i] = TOMBSTONE;                  // not null: later keys may probe past here
        vals[i] = null;
        size--;
        tombstones++;
        return old;
    }

    @SuppressWarnings("unchecked") // keys holds only null, TOMBSTONE, or keys of type K
    private void resize(int newCapacity) {
        Object[] oldKeys = keys;
        Object[] oldVals = vals;
        keys = new Object[newCapacity];
        vals = new Object[newCapacity];
        size = 0;
        tombstones = 0;
        for (int i = 0; i < oldKeys.length; i++) {
            if (oldKeys[i] != null && oldKeys[i] != TOMBSTONE) {
                put((K) oldKeys[i], (V) oldVals[i]);   // rehash live keys only
            }
        }
    }

    /** The slots visited by a search for key, in probe order. */
    public List<Integer> probeSequence(Object key) {
        List<Integer> out = new ArrayList<>();
        int m = keys.length;
        for (int j = 0, i = home(key); j < m; j++, i = (i + 1) % m) {
            out.add(i);
            if (keys[i] == null || (keys[i] != TOMBSTONE && keys[i].equals(key))) {
                break;
            }
        }
        return out;
    }

    public int size() {
        return size;
    }

    public int capacity() {
        return keys.length;
    }

    public int tombstones() {
        return tombstones;
    }

    @SuppressWarnings("unchecked") // keys holds only null, TOMBSTONE, or keys of type K
    public List<K> keys() {
        List<K> out = new ArrayList<>();
        for (Object k : keys) {
            if (k != null && k != TOMBSTONE) {
                out.add((K) k);
            }
        }
        return out;
    }

    /** The slots as text, e.g. "[_, _, 44, 23, 88, 12, 13]" with X for a tombstone. */
    public String slotString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < keys.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            Object k = keys[i];
            sb.append(k == null ? "_" : k == TOMBSTONE ? "X" : k.toString());
        }
        return sb.append(']').toString();
    }
}
