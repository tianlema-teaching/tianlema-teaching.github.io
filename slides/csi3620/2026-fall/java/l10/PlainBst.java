package l10;

/** An unbalanced binary search tree of distinct int keys, used only to show how height can grow. */
public final class PlainBst {

    private static final class Node {
        final int key;
        Node left, right;

        Node(int key) { this.key = key; }
    }

    private Node root;

    /** Iterative insert, so a tall tree cannot overflow the call stack. */
    public boolean insert(int key) {
        if (root == null) { root = new Node(key); return true; }
        Node n = root;
        while (true) {
            if (key == n.key) return false;
            if (key < n.key) {
                if (n.left == null) { n.left = new Node(key); return true; }
                n = n.left;
            } else {
                if (n.right == null) { n.right = new Node(key); return true; }
                n = n.right;
            }
        }
    }

    /** Height in edges: -1 for an empty tree. Iterative level-by-level count. */
    public int height() {
        java.util.ArrayDeque<Node> level = new java.util.ArrayDeque<>();
        if (root != null) level.add(root);
        int h = -1;
        while (!level.isEmpty()) {
            h++;
            for (int i = level.size(); i > 0; i--) {
                Node n = level.poll();
                if (n.left != null) level.add(n.left);
                if (n.right != null) level.add(n.right);
            }
        }
        return h;
    }
}
