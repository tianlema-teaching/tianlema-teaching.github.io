package l10;

import java.util.ArrayList;
import java.util.List;

/**
 * An AVL tree storing distinct keys (a sorted set).
 * Height counts edges: an empty tree has height -1 and a single node has height 0.
 * Balance factor = height(left) - height(right); AVL requires it to be -1, 0 or +1 at every node.
 */
public final class AvlTree<K extends Comparable<K>> {

    static final class Node<K> {
        K key;
        Node<K> left, right;
        int height;              // stored height of the subtree rooted here

        Node(K key) {
            this.key = key;      // a new node is a leaf: height 0
        }
    }

    private Node<K> root;
    private int size;
    private final List<String> events = new ArrayList<>();   // rebalancing log, e.g. "LR at 30"

    public int size() { return size; }

    public boolean isEmpty() { return size == 0; }

    public int height() { return height(root); }

    static int height(Node<?> n) {
        return n == null ? -1 : n.height;
    }

    private static void update(Node<?> n) {
        n.height = 1 + Math.max(height(n.left), height(n.right));
    }

    static int balance(Node<?> n) {
        return height(n.left) - height(n.right);
    }

    /** Rotate x's right child y up; returns y, the new subtree root. */
    private static <K> Node<K> rotateLeft(Node<K> x) {
        Node<K> y = x.right;
        x.right = y.left;        // y's left subtree moves under x
        y.left = x;
        update(x);               // x is now below y: update it first
        update(y);
        return y;
    }

    /** Rotate y's left child x up; returns x, the new subtree root. */
    private static <K> Node<K> rotateRight(Node<K> y) {
        Node<K> x = y.left;
        y.left = x.right;        // x's right subtree moves under y
        x.right = y;
        update(y);
        update(x);
        return x;
    }

    /** Restore the AVL property at node, whose subtrees are AVL trees. */
    private Node<K> rebalance(Node<K> node) {
        update(node);
        int bf = balance(node);
        if (bf > 1) {                            // left side too tall
            if (balance(node.left) < 0) {        // LR: first rotate the child
                events.add("LR at " + node.key);
                node.left = rotateLeft(node.left);
                snapshot();
            } else {
                events.add("LL at " + node.key);
            }
            return rotateRight(node);
        }
        if (bf < -1) {                           // right side too tall
            if (balance(node.right) > 0) {       // RL: first rotate the child
                events.add("RL at " + node.key);
                node.right = rotateRight(node.right);
                snapshot();
            } else {
                events.add("RR at " + node.key);
            }
            return rotateLeft(node);
        }
        return node;                             // already balanced
    }

    /** Insert key; returns false if it was already present. */
    public boolean insert(K key) {
        if (key == null) throw new NullPointerException("null key");
        int before = size;
        root = insert(root, key);
        return size > before;
    }

    private Node<K> insert(Node<K> node, K key) {
        if (node == null) {
            size++;
            return new Node<>(key);
        }
        int c = key.compareTo(node.key);
        if (c < 0) node.left = insert(node.left, key);
        else if (c > 0) node.right = insert(node.right, key);
        else return node;                        // duplicate: no change
        return rebalance(node);
    }

    /** Delete key; returns false if it was absent. */
    public boolean delete(K key) {
        if (key == null) throw new NullPointerException("null key");
        int before = size;
        root = delete(root, key);
        return size < before;
    }

    private Node<K> delete(Node<K> node, K key) {
        if (node == null) return null;           // key not found
        int c = key.compareTo(node.key);
        if (c < 0) node.left = delete(node.left, key);
        else if (c > 0) node.right = delete(node.right, key);
        else if (node.left == null || node.right == null) {
            size--;                              // zero or one child: splice out
            return node.left != null ? node.left : node.right;
        } else {
            Node<K> s = node.right;              // two children: successor
            while (s.left != null) s = s.left;
            node.key = s.key;                    // copy successor key up
            node.right = delete(node.right, s.key);
        }
        return rebalance(node);                  // every ancestor on the path
    }

    public boolean contains(K key) {
        Node<K> n = root;
        while (n != null) {
            int c = key.compareTo(n.key);
            if (c == 0) return true;
            n = c < 0 ? n.left : n.right;
        }
        return false;
    }

    /** Keys in sorted order (inorder traversal). */
    public List<K> keys() {
        List<K> out = new ArrayList<>();
        inorder(root, out);
        return out;
    }

    private static <K> void inorder(Node<K> n, List<K> out) {
        if (n == null) return;
        inorder(n.left, out);
        out.add(n.key);
        inorder(n.right, out);
    }

    /** Rebalancing events since the last call, then clears the log. */
    public List<String> drainEvents() {
        List<String> copy = new ArrayList<>(events);
        events.clear();
        return copy;
    }

    // ---- tracing: the shape of the tree as text, e.g. 20(10,30(-,40)) ----

    private List<String> trace;

    /** Record the tree shape at the midpoint of every double rotation into sink (null stops). */
    public void setTrace(List<String> sink) { trace = sink; }

    private void snapshot() {
        if (trace != null) trace.add(shape());
    }

    public String shape() { return shape(root); }

    private static String shape(Node<?> n) {
        if (n == null) return "-";
        if (n.left == null && n.right == null) return String.valueOf(n.key);
        return n.key + "(" + shape(n.left) + "," + shape(n.right) + ")";
    }

    // ---- checker: BST order, correct stored heights, AVL balance ----

    /** Throws AssertionError if any invariant fails. */
    public void check() {
        int[] count = {0};
        check(root, null, null, count);
        if (count[0] != size) throw new AssertionError("size " + size + " but " + count[0] + " nodes");
    }

    private int check(Node<K> n, K lo, K hi, int[] count) {
        if (n == null) return -1;
        count[0]++;
        boolean inRange = (lo == null || n.key.compareTo(lo) > 0)
                && (hi == null || n.key.compareTo(hi) < 0);
        if (!inRange) throw new AssertionError("order broken at " + n.key);
        int hl = check(n.left, lo, n.key, count);
        int hr = check(n.right, n.key, hi, count);
        if (n.height != 1 + Math.max(hl, hr))
            throw new AssertionError("stale height at " + n.key);
        if (Math.abs(hl - hr) > 1)
            throw new AssertionError("unbalanced at " + n.key);
        return n.height;
    }
}
