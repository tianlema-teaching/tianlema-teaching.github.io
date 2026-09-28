package l10;

/** Height bounds for AVL trees, with height counted in edges. */
public final class AvlBounds {

    private AvlBounds() { }

    /** N(h): fewest nodes in an AVL tree of height h. */
    public static long minNodes(int h) {
        long older = 0, old = 1;                // N(-1) = 0 (empty), N(0) = 1
        if (h < 0) return older;
        for (int i = 1; i <= h; i++) {
            long next = old + older + 1;        // root + tallest child + other child
            older = old;
            old = next;
        }
        return old;
    }

    /** Fibonacci numbers with F(1) = F(2) = 1. */
    public static long fib(int k) {
        long a = 0, b = 1;                      // F(0), F(1)
        for (int i = 0; i < k; i++) {
            long t = a + b;
            a = b;
            b = t;
        }
        return a;
    }

    /** Largest possible AVL height for n nodes: the largest h with N(h) <= n. */
    public static int maxHeight(long n) {
        int h = -1;
        while (minNodes(h + 1) <= n) h++;
        return h;
    }
}
