package l09;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/** Traversal-shaped computations on binary trees. Height counts edges; empty is -1. */
public final class TreeOps {

    private TreeOps() {
    }

    /** Preorder: make the parent, then copy its subtrees into it. */
    static <T> BinaryNode<T> copy(BinaryNode<T> x) {
        if (x == null) return null;
        BinaryNode<T> c = new BinaryNode<>(x.value);  // parent first,
        c.left = copy(x.left);                    // then its children
        c.right = copy(x.right);
        return c;
    }

    /** Postorder: both subtree sizes are needed before x's size. */
    static <T> int size(BinaryNode<T> x) {
        if (x == null) return 0;
        return size(x.left) + size(x.right) + 1;
    }

    static <T> int height(BinaryNode<T> x) {
        if (x == null) return -1;
        return 1 + Math.max(height(x.left), height(x.right));
    }

    /** Same shape and values. */
    static <T> boolean sameTree(BinaryNode<T> a, BinaryNode<T> b) {
        if (a == null || b == null) return a == b;
        return a.value.equals(b.value) && sameTree(a.left, b.left) && sameTree(a.right, b.right);
    }

    /** The values level by level, each level left to right. */
    static <T> List<List<T>> byLevels(BinaryNode<T> root) {
        List<List<T>> levels = new ArrayList<>();
        Deque<BinaryNode<T>> queue = new ArrayDeque<>();
        if (root != null) queue.add(root);
        while (!queue.isEmpty()) {
            List<T> level = new ArrayList<>();
            for (int k = queue.size(); k > 0; k--) {  // exactly one level
                BinaryNode<T> x = queue.remove();
                level.add(x.value);
                if (x.left != null) queue.add(x.left);
                if (x.right != null) queue.add(x.right);
            }
            levels.add(level);
        }
        return levels;
    }

    /** Maximum number of nodes on one level (0 for the empty tree). */
    static <T> int maxWidth(BinaryNode<T> root) {
        int w = 0;
        for (List<T> level : byLevels(root)) w = Math.max(w, level.size());
        return w;
    }
}
