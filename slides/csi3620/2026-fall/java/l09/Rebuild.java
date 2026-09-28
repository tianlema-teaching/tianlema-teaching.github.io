package l09;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Rebuilds a binary tree from its preorder and inorder sequences. The values must be distinct and
 * both sequences must come from one tree; otherwise it throws IllegalArgumentException.
 */
public final class Rebuild {

    private Rebuild() {
    }

    static <T> BinaryNode<T> fromPreIn(List<T> pre, List<T> in) {
        if (pre.size() != in.size()) throw new IllegalArgumentException("lengths differ");
        if (new HashSet<>(pre).size() != pre.size()) throw new IllegalArgumentException("values must be distinct");
        Map<T, Integer> where = new HashMap<>();     // value -> inorder index
        for (int i = 0; i < in.size(); i++) where.put(in.get(i), i);
        if (where.size() != in.size()) throw new IllegalArgumentException("values must be distinct");
        for (T v : pre) if (!where.containsKey(v)) throw new IllegalArgumentException("mismatch: " + v);
        return build(pre, 0, where, 0, in.size() - 1);
    }

    /** Subtree whose preorder starts at pre[p] and whose inorder is in[lo..hi]. */
    private static <T> BinaryNode<T> build(List<T> pre, int p,
            Map<T, Integer> where, int lo, int hi) {
        if (lo > hi) return null;         // empty range: no subtree
        T rootValue = pre.get(p);         // preorder lists the root first
        int m = where.get(rootValue);     // inorder splits at the root
        if (m < lo || m > hi)
            throw new IllegalArgumentException("sequences are not from one tree");
        int leftSize = m - lo;
        BinaryNode<T> x = new BinaryNode<>(rootValue);
        x.left = build(pre, p + 1, where, lo, m - 1);
        x.right = build(pre, p + 1 + leftSize, where, m + 1, hi);
        return x;
    }
}
