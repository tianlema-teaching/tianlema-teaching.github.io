package l08;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * An unbalanced binary search tree of distinct keys.
 * Duplicates are ignored: inserting a key that is already present changes nothing.
 * Height counts edges: an empty tree has height -1, a single node height 0.
 * Every public method that takes a key throws NullPointerException for a null key.
 */
public class BST<K extends Comparable<? super K>> {

    static final class Node<K> {
        K key;
        Node<K> left, right;

        Node(K key) {
            this.key = key;
        }
    }

    private Node<K> root;
    private int size;

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return root == null;
    }

    /** Recursive search: true when key is in the tree. */
    public boolean contains(K key) {
        Objects.requireNonNull(key, "null key");
        return contains(root, key);
    }

    private boolean contains(Node<K> x, K key) {
        if (x == null) return false;              // fell off the tree: absent
        int c = key.compareTo(x.key);
        if (c == 0) return true;
        if (c < 0) return contains(x.left, key);  // smaller keys live left
        return contains(x.right, key);            // larger keys live right
    }

    /** Iterative search: the list of keys compared, in order (the search path). */
    public List<K> searchPath(K key) {
        Objects.requireNonNull(key, "null key");
        List<K> path = new ArrayList<>();
        Node<K> x = root;
        while (x != null) {
            path.add(x.key);
            int c = key.compareTo(x.key);
            if (c == 0) break;
            x = (c < 0) ? x.left : x.right;
        }
        return path;
    }

    /** Inserts key; returns false and changes nothing if key is already present. */
    public boolean insert(K key) {
        Objects.requireNonNull(key, "null key");
        int before = size;
        root = insert(root, key);
        return size > before;
    }

    private Node<K> insert(Node<K> x, K key) {
        if (x == null) {                          // empty spot: new leaf
            size++;
            return new Node<>(key);
        }
        int c = key.compareTo(x.key);
        if (c < 0) x.left = insert(x.left, key);
        else if (c > 0) x.right = insert(x.right, key);
        // c == 0: duplicate, ignore
        return x;
    }

    public K min() {
        if (root == null) throw new IllegalStateException("empty tree");
        Node<K> x = root;
        while (x.left != null) x = x.left;        // keep going left
        return x.key;
    }

    public K max() {
        if (root == null) throw new IllegalStateException("empty tree");
        Node<K> x = root;
        while (x.right != null) x = x.right;      // keep going right
        return x.key;
    }

    /** Smallest key strictly greater than key, or null if there is none. */
    public K successor(K key) {
        Objects.requireNonNull(key, "null key");
        Node<K> x = root, best = null;
        while (x != null) {
            if (key.compareTo(x.key) < 0) {
                best = x;                         // x is larger: a candidate
                x = x.left;                       // look for a smaller one
            } else {
                x = x.right;                      // x too small: go right
            }
        }
        return best == null ? null : best.key;
    }

    /** Removes key if present; returns true when something was removed. */
    public boolean delete(K key) {
        Objects.requireNonNull(key, "null key");
        int before = size;
        root = delete(root, key);
        return size < before;
    }

    private Node<K> delete(Node<K> x, K key) {
        if (x == null) return null;               // key absent
        int c = key.compareTo(x.key);
        if (c < 0) { x.left = delete(x.left, key); return x; }
        if (c > 0) { x.right = delete(x.right, key); return x; }
        size--;
        if (x.left == null) return x.right;       // leaf or right child only
        if (x.right == null) return x.left;       // left child only
        Node<K> s = minNode(x.right);             // in-order successor
        s.right = deleteMin(x.right);             // unhook s from its spot
        s.left = x.left;
        return s;                                 // s takes x's place
    }

    private Node<K> minNode(Node<K> x) {
        while (x.left != null) x = x.left;
        return x;
    }

    private Node<K> deleteMin(Node<K> x) {
        if (x.left == null) return x.right;       // x is the minimum
        x.left = deleteMin(x.left);
        return x;
    }

    /** Height in edges; -1 for the empty tree. */
    public int height() {
        return height(root);
    }

    private int height(Node<K> x) {
        if (x == null) return -1;
        return 1 + Math.max(height(x.left), height(x.right));
    }

    /** Depth of key in edges from the root, or -1 if absent. */
    public int depth(K key) {
        Objects.requireNonNull(key, "null key");
        List<K> path = searchPath(key);
        if (path.isEmpty() || path.get(path.size() - 1).compareTo(key) != 0) return -1;
        return path.size() - 1;
    }

    /** Keys in sorted order (an in-order walk; Lecture 9 explains why). */
    public List<K> keys() {
        List<K> out = new ArrayList<>();
        collect(root, out);
        return out;
    }

    private void collect(Node<K> x, List<K> out) {
        if (x == null) return;
        collect(x.left, out);
        out.add(x.key);
        collect(x.right, out);
    }

    /** Parenthesized shape, e.g. "50(30(20,40),70(-,80))"; "-" marks a missing child. */
    public String shape() {
        return root == null ? "-" : shape(root);
    }

    private String shape(Node<K> x) {
        if (x == null) return "-";
        if (x.left == null && x.right == null) return String.valueOf(x.key);
        return x.key + "(" + shape(x.left) + "," + shape(x.right) + ")";
    }

    /** Checks the BST property with open bounds; used by the tests. */
    boolean isValid() {
        return isValid(root, null, null);
    }

    private boolean isValid(Node<K> x, K lo, K hi) {
        if (x == null) return true;
        if (lo != null && x.key.compareTo(lo) <= 0) return false;
        if (hi != null && x.key.compareTo(hi) >= 0) return false;
        return isValid(x.left, lo, x.key) && isValid(x.right, x.key, hi);
    }
}
