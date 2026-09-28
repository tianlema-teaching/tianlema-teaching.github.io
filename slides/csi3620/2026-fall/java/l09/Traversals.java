package l09;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

/** Depth-first and breadth-first traversals of a binary tree. */
public final class Traversals {

    private Traversals() {
    }

    static <T> void preorder(BinaryNode<T> x, List<T> out) {
        if (x == null) return;
        out.add(x.value);                         // visit before the children
        preorder(x.left, out);
        preorder(x.right, out);
    }

    static <T> void inorder(BinaryNode<T> x, List<T> out) {
        if (x == null) return;
        inorder(x.left, out);
        out.add(x.value);                         // visit between the children
        inorder(x.right, out);
    }

    static <T> void postorder(BinaryNode<T> x, List<T> out) {
        if (x == null) return;
        postorder(x.left, out);
        postorder(x.right, out);
        out.add(x.value);                         // visit after the children
    }

    /** Records each node three times: on arrival, between its subtrees, on leaving. */
    static <T> void eulerTour(BinaryNode<T> x, List<String> out) {
        if (x == null) return;
        out.add("pre " + x.value);                // 1st: arriving from above
        eulerTour(x.left, out);
        out.add("in " + x.value);                 // 2nd: back from the left
        eulerTour(x.right, out);
        out.add("post " + x.value);               // 3rd: back from the right
    }

    /** Level order with a FIFO queue. If log is not null, records "visited | queue" per step. */
    static <T> List<T> levelOrder(BinaryNode<T> root, List<String> log) {
        List<T> out = new ArrayList<>();
        if (root == null) return out;
        Deque<BinaryNode<T>> queue = new ArrayDeque<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            BinaryNode<T> x = queue.remove();     // take from the front
            out.add(x.value);
            if (x.left != null) queue.add(x.left);    // children join the back
            if (x.right != null) queue.add(x.right);
            if (log != null) log.add(x.value + " | " + frontToBack(queue));
        }
        return out;
    }

    /** Inorder with an explicit stack. If log is not null, records "visited | stack" per step. */
    static <T> List<T> inorderIterative(BinaryNode<T> root, List<String> log) {
        List<T> out = new ArrayList<>();
        Deque<BinaryNode<T>> stack = new ArrayDeque<>();
        BinaryNode<T> x = root;
        while (x != null || !stack.isEmpty()) {
            while (x != null) {                   // walk left, saving the path
                stack.push(x);
                x = x.left;
            }
            x = stack.pop();                      // leftmost unvisited node
            out.add(x.value);
            if (log != null) log.add(x.value + " | " + bottomToTop(stack));
            x = x.right;                          // then its right subtree
        }
        return out;
    }

    /** Queue contents, front first. */
    static <T> String frontToBack(Deque<BinaryNode<T>> q) {
        List<T> vs = new ArrayList<>();
        for (BinaryNode<T> n : q) vs.add(n.value);
        return vs.toString();
    }

    /** Stack contents, bottom first (the top is the last element). */
    static <T> String bottomToTop(Deque<BinaryNode<T>> s) {
        List<T> vs = new ArrayList<>();
        for (Iterator<BinaryNode<T>> it = s.descendingIterator(); it.hasNext(); ) vs.add(it.next().value);
        return vs.toString();
    }
}
