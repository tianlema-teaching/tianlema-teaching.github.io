package l13;

/** Checks every B-tree property; throws AssertionError naming the first one that fails. */
public final class BTreeValidator {

    private BTreeValidator() {
    }

    public static <K extends Comparable<? super K>> void validate(BTree<K> tree) {
        int t = tree.minDegree();
        int[] leafDepth = {-1};
        int count = walk(tree.root(), null, null, 0, true, t, leafDepth);
        if (count != tree.size()) {
            throw new AssertionError("size " + tree.size() + " but " + count + " keys stored");
        }
        if (leafDepth[0] != tree.height()) {
            throw new AssertionError("height does not match leaf depth");
        }
    }

    /** Returns the number of keys in the subtree; lo and hi are exclusive bounds or null. */
    private static <K extends Comparable<? super K>> int walk(BTree.Node<K> x, K lo, K hi,
            int depth, boolean isRoot, int t, int[] leafDepth) {
        int n = x.keys.size();
        if (n > 2 * t - 1) {
            throw new AssertionError("node has more than 2t-1 keys: " + x.keys);
        }
        if (!isRoot && n < t - 1) {
            throw new AssertionError("non-root node has fewer than t-1 keys: " + x.keys);
        }
        if (isRoot && n == 0 && !x.isLeaf()) {
            throw new AssertionError("empty internal root");
        }
        for (int i = 0; i < n; i++) {
            K k = x.keys.get(i);
            if (i > 0 && x.keys.get(i - 1).compareTo(k) >= 0) {
                throw new AssertionError("keys not strictly increasing: " + x.keys);
            }
            if ((lo != null && k.compareTo(lo) <= 0) || (hi != null && k.compareTo(hi) >= 0)) {
                throw new AssertionError("key " + k + " outside its separator range");
            }
        }
        if (x.isLeaf()) {
            if (leafDepth[0] == -1) {
                leafDepth[0] = depth;
            } else if (leafDepth[0] != depth) {
                throw new AssertionError("leaves at different depths");
            }
            return n;
        }
        if (x.children.size() != n + 1) {
            throw new AssertionError("internal node with " + n + " keys has "
                    + x.children.size() + " children");
        }
        int count = n;
        for (int i = 0; i <= n; i++) {
            K childLo = i == 0 ? lo : x.keys.get(i - 1);
            K childHi = i == n ? hi : x.keys.get(i);
            count += walk(x.children.get(i), childLo, childHi, depth + 1, false, t, leafDepth);
        }
        return count;
    }
}
