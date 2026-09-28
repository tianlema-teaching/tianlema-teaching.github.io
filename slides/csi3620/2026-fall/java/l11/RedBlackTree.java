package l11;

import java.util.ArrayList;
import java.util.List;

/**
 * A red-black tree storing distinct keys (a sorted set), following CLRS Chapter 13:
 * one shared black sentinel {@code nil} stands for every leaf (NIL) and for the root's parent.
 * Only search and insertion are implemented; deletion is left out on purpose.
 */
public final class RedBlackTree<K extends Comparable<K>> {

    private static final boolean RED = true;
    private static final boolean BLACK = false;

    final class Node {
        K key;
        boolean color;
        Node left, right, parent;

        Node(K key, boolean color) {
            this.key = key;
            this.color = color;
        }
    }

    private final Node nil;      // the sentinel: always black
    private Node root;
    private int size;
    private final List<String> events = new ArrayList<>();   // fix-up log, e.g. "case 1 at 20"

    public RedBlackTree() {
        nil = new Node(null, BLACK);
        nil.left = nil.right = nil.parent = nil;
        root = nil;
    }

    public int size() { return size; }

    /** The root node, for tests in this package; NIL's node when the tree is empty. */
    Node rootNode() { return root; }

    public boolean isEmpty() { return size == 0; }

    private void leftRotate(Node x) {
        Node y = x.right;
        x.right = y.left;                        // y's left subtree moves under x
        if (y.left != nil) y.left.parent = x;
        y.parent = x.parent;                     // y takes x's place
        if (x.parent == nil) root = y;
        else if (x == x.parent.left) x.parent.left = y;
        else x.parent.right = y;
        y.left = x;                              // x goes below y
        x.parent = y;
    }

    private void rightRotate(Node y) {
        Node x = y.left;
        y.left = x.right;                        // x's right subtree moves under y
        if (x.right != nil) x.right.parent = y;
        x.parent = y.parent;                     // x takes y's place
        if (y.parent == nil) root = x;
        else if (y == y.parent.right) y.parent.right = x;
        else y.parent.left = x;
        x.right = y;                             // y goes below x
        y.parent = x;
    }

    /** Insert key; returns false if it was already present. */
    public boolean insert(K key) {
        if (key == null) throw new NullPointerException("null key");
        Node y = nil;
        Node x = root;
        while (x != nil) {                       // ordinary BST descent
            y = x;
            int c = key.compareTo(x.key);
            if (c == 0) return false;            // duplicate: no change
            x = c < 0 ? x.left : x.right;
        }
        Node z = new Node(key, RED);             // new nodes start red
        z.parent = y;
        z.left = z.right = nil;
        if (y == nil) root = z;
        else if (key.compareTo(y.key) < 0) y.left = z;
        else y.right = z;
        size++;
        insertFixup(z);
        return true;
    }

    private void insertFixup(Node z) {
        while (z.parent.color == RED) {  // red parent: property 4 broken
            Node g = z.parent.parent;    // exists: red parent is not root
            if (z.parent == g.left) {
                Node uncle = g.right;
                if (uncle.color == RED) {  // case 1: recolor, move up two levels
                    events.add("case 1 at " + z.key);
                    z.parent.color = BLACK;
                    uncle.color = BLACK;
                    g.color = RED;
                    z = g;
                } else {
                    if (z == z.parent.right) {   // case 2: triangle, rotate into a line
                        events.add("case 2 at " + z.key);
                        z = z.parent;
                        leftRotate(z);
                        snapshot();
                    }
                    events.add("case 3 at " + z.key);  // case 3: line
                    z.parent.color = BLACK;
                    g.color = RED;
                    rightRotate(g);
                }
            } else {                             // mirror image: parent is a right child
                Node uncle = g.left;
                if (uncle.color == RED) {        // case 1 (mirror)
                    events.add("mirror case 1 at " + z.key);
                    z.parent.color = BLACK;
                    uncle.color = BLACK;
                    g.color = RED;
                    z = g;
                } else {
                    if (z == z.parent.left) {    // case 2 (mirror)
                        events.add("mirror case 2 at " + z.key);
                        z = z.parent;
                        rightRotate(z);
                        snapshot();
                    }
                    events.add("mirror case 3 at " + z.key);  // case 3 (mirror)
                    z.parent.color = BLACK;
                    g.color = RED;
                    leftRotate(g);
                }
            }
            snapshot();
        }
        root.color = BLACK;                      // property 2
    }

    public boolean contains(K key) {
        Node x = root;
        while (x != nil) {
            int c = key.compareTo(x.key);
            if (c == 0) return true;
            x = c < 0 ? x.left : x.right;
        }
        return false;
    }

    /** Height in edges of the longest root-to-node path; -1 for an empty tree. */
    public int height() { return height(root); }

    private int height(Node x) {
        return x == nil ? -1 : 1 + Math.max(height(x.left), height(x.right));
    }

    /** Keys in sorted order (inorder traversal). */
    public List<K> keys() {
        List<K> out = new ArrayList<>();
        inorder(root, out);
        return out;
    }

    private void inorder(Node x, List<K> out) {
        if (x == nil) return;
        inorder(x.left, out);
        out.add(x.key);
        inorder(x.right, out);
    }

    /** Fix-up events since the last call, then clears the log. */
    public List<String> drainEvents() {
        List<String> copy = new ArrayList<>(events);
        events.clear();
        return copy;
    }

    // ---- tracing: the tree as text, e.g. B20(R10,R30); "-" is a NIL leaf ----

    private List<String> trace;

    /** Record the tree after every fix-up step (case 2 rotation, case 1 or case 3) into sink. */
    public void setTrace(List<String> sink) { trace = sink; }

    private void snapshot() {
        if (trace != null) trace.add(shape());
    }

    public String shape() { return shape(root); }

    private String shape(Node x) {
        if (x == nil) return "-";
        String s = (x.color == RED ? "R" : "B") + x.key;
        if (x.left == nil && x.right == nil) return s;
        return s + "(" + shape(x.left) + "," + shape(x.right) + ")";
    }

    // ---- the same keys read as a 2-3-4 tree: each black node absorbs its red children ----

    /** For example B20(B10,R50(B30,B60)) reads as [20 50]([10],[30],[60]). */
    public String asTwoThreeFour() {
        return root == nil ? "-" : group(root);
    }

    private String group(Node b) {               // b is black
        List<K> keys = new ArrayList<>();
        List<Node> kids = new ArrayList<>();
        absorb(b.left, keys, kids);
        keys.add(b.key);
        absorb(b.right, keys, kids);
        StringBuilder s = new StringBuilder("[");
        for (int i = 0; i < keys.size(); i++) s.append(i > 0 ? " " : "").append(keys.get(i));
        s.append("]");
        if (kids.get(0) == nil) return s.toString();   // property 5: then every child is NIL
        s.append("(");
        for (int i = 0; i < kids.size(); i++) s.append(i > 0 ? "," : "").append(group(kids.get(i)));
        return s.append(")").toString();
    }

    private void absorb(Node x, List<K> keys, List<Node> kids) {
        if (x.color == RED) {                    // a red child joins its black parent's group
            kids.add(x.left);                    // property 4: its children are black
            keys.add(x.key);
            kids.add(x.right);
        } else {
            kids.add(x);                         // a black child (or NIL) starts a new group
        }
    }

    // ---- validator: BST order, parent links and the five red-black properties ----

    /** Throws AssertionError if any property fails; returns bh(root), CLRS black-height. */
    public int check() {
        // Property 1 (every node is red or black) holds by construction: color is a boolean.
        if (root.color != BLACK) throw new AssertionError("property 2: root is red");
        if (nil.color != BLACK) throw new AssertionError("property 3: NIL is red");
        if (root.parent != nil) throw new AssertionError("root parent is not NIL");
        int[] count = {0};
        int bh = check(root, null, null, count) - 1;   // bh(root) does not count the root
        if (count[0] != size) throw new AssertionError("size " + size + " but " + count[0] + " nodes");
        return bh;
    }

    /** Returns the black nodes on every path from x down to a NIL, counting x and the NIL. */
    private int check(Node x, K lo, K hi, int[] count) {
        if (x == nil) return 1;                  // property 3: the NIL leaf is black
        count[0]++;
        boolean inRange = (lo == null || x.key.compareTo(lo) > 0)
                && (hi == null || x.key.compareTo(hi) < 0);
        if (!inRange) throw new AssertionError("order broken at " + x.key);
        if ((x.left != nil && x.left.parent != x) || (x.right != nil && x.right.parent != x))
            throw new AssertionError("bad parent link at " + x.key);
        if (x.color == RED && (x.left.color == RED || x.right.color == RED))
            throw new AssertionError("property 4: red " + x.key + " has a red child");
        int bl = check(x.left, lo, x.key, count);
        int br = check(x.right, x.key, hi, count);
        if (bl != br)
            throw new AssertionError("property 5: black counts differ below " + x.key);
        return bl + (x.color == BLACK ? 1 : 0);
    }
}
