package l13;

import java.util.ArrayList;
import java.util.List;

/**
 * A B-tree of distinct keys with minimum degree t (CLRS convention): every node except the
 * root holds t-1 to 2t-1 keys. Insertion splits full nodes on the way down.
 */
public class BTree<K extends Comparable<? super K>> {

    static final class Node<K> {
        final List<K> keys = new ArrayList<>();
        final List<Node<K>> children = new ArrayList<>();   // empty for a leaf

        boolean isLeaf() {
            return children.isEmpty();
        }
    }

    private final int t;
    private Node<K> root = new Node<>();
    private int size;

    public BTree(int t) {
        if (t < 2) {
            throw new IllegalArgumentException("minimum degree t must be at least 2");
        }
        this.t = t;
    }

    public boolean contains(K key) {
        Node<K> x = root;
        while (true) {
            int i = 0;
            while (i < x.keys.size() && key.compareTo(x.keys.get(i)) > 0) {
                i++;                          // first key >= the search key
            }
            if (i < x.keys.size() && key.compareTo(x.keys.get(i)) == 0) {
                return true;
            }
            if (x.isLeaf()) {
                return false;
            }
            x = x.children.get(i);            // descend between keys i-1 and i
        }
    }

    /** Inserts key; returns false (and changes nothing) if it is already present. */
    public boolean insert(K key) {
        if (key == null) {
            throw new NullPointerException("null keys are not supported");
        }
        if (contains(key)) {
            return false;
        }
        if (root.keys.size() == 2 * t - 1) {  // full root: the tree grows by one level
            Node<K> s = new Node<>();
            s.children.add(root);
            root = s;
            splitChild(s, 0);
        }
        insertNonFull(root, key);
        size++;
        return true;
    }

    /** Splits the full child x.children[i] around its median key, which moves up into x. */
    private void splitChild(Node<K> x, int i) {
        Node<K> y = x.children.get(i);
        Node<K> z = new Node<>();
        z.keys.addAll(y.keys.subList(t, 2 * t - 1));          // upper t-1 keys
        if (!y.isLeaf()) {
            z.children.addAll(y.children.subList(t, 2 * t));  // upper t children
            y.children.subList(t, 2 * t).clear();
        }
        K median = y.keys.get(t - 1);
        y.keys.subList(t - 1, 2 * t - 1).clear();             // y keeps t-1 keys
        x.keys.add(i, median);
        x.children.add(i + 1, z);
    }

    /** Inserts key into the subtree at x, which is not full. */
    private void insertNonFull(Node<K> x, K key) {
        while (!x.isLeaf()) {
            int i = 0;
            while (i < x.keys.size() && key.compareTo(x.keys.get(i)) > 0) {
                i++;
            }
            if (x.children.get(i).keys.size() == 2 * t - 1) {
                splitChild(x, i);             // split before descending
                if (key.compareTo(x.keys.get(i)) > 0) {
                    i++;                      // the key belongs right of the new median
                }
            }
            x = x.children.get(i);
        }
        int i = 0;
        while (i < x.keys.size() && key.compareTo(x.keys.get(i)) > 0) {
            i++;
        }
        x.keys.add(i, key);                   // leaf: insert in sorted position
    }

    /** All keys in increasing order. */
    public List<K> inOrder() {
        List<K> out = new ArrayList<>();
        inOrder(root, out);
        return out;
    }

    private void inOrder(Node<K> x, List<K> out) {
        for (int i = 0; i < x.keys.size(); i++) {
            if (!x.isLeaf()) {
                inOrder(x.children.get(i), out);   // subtree left of key i
            }
            out.add(x.keys.get(i));
        }
        if (!x.isLeaf()) {
            inOrder(x.children.get(x.keys.size()), out);   // rightmost subtree
        }
    }

    public int size() {
        return size;
    }

    public int minDegree() {
        return t;
    }

    /** Height in edges from the root to a leaf; a lone root has height 0. */
    public int height() {
        int h = 0;
        for (Node<K> x = root; !x.isLeaf(); x = x.children.get(0)) {
            h++;
        }
        return h;
    }

    /** Number of nodes a search for key reads, from the root down. */
    public int nodesRead(K key) {
        int reads = 0;
        Node<K> x = root;
        while (true) {
            reads++;
            int i = 0;
            while (i < x.keys.size() && key.compareTo(x.keys.get(i)) > 0) {
                i++;
            }
            if ((i < x.keys.size() && key.compareTo(x.keys.get(i)) == 0) || x.isLeaf()) {
                return reads;
            }
            x = x.children.get(i);
        }
    }

    Node<K> root() {
        return root;
    }

    /** The tree level by level, e.g. "[20] / [5 12] [30 40]". */
    public String levels() {
        StringBuilder sb = new StringBuilder();
        List<Node<K>> level = List.of(root);
        while (!level.isEmpty()) {
            List<Node<K>> next = new ArrayList<>();
            StringBuilder line = new StringBuilder();
            for (Node<K> x : level) {
                if (line.length() > 0) {
                    line.append(' ');
                }
                line.append('[');
                for (int i = 0; i < x.keys.size(); i++) {
                    line.append(i == 0 ? "" : " ").append(x.keys.get(i));
                }
                line.append(']');
                next.addAll(x.children);
            }
            if (sb.length() > 0) {
                sb.append(" / ");
            }
            sb.append(line);
            level = next;
        }
        return sb.toString();
    }
}
