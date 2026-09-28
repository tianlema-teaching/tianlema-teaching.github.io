package l13;

/** Prints the states of the running example that the slides draw. */
public final class Trace {

    static final int[] KEYS = {10, 20, 30, 40, 50, 60, 70, 80, 90, 25};

    private Trace() {
    }

    public static void main(String[] args) {
        BTree<Integer> tree = new BTree<>(2);
        for (int k : KEYS) {
            tree.insert(k);
            System.out.println("insert " + k + ": " + tree.levels() + "  height " + tree.height());
        }
        BTree<Integer> quiz = new BTree<>(2);
        for (int k : KEYS) {
            quiz.insert(k);
        }
        quiz.insert(100);
        System.out.println("quiz, insert 100: " + quiz.levels());
        for (int k : new int[] {25, 90, 35}) {
            System.out.println("search " + k + " reads " + tree.nodesRead(k) + " found " + tree.contains(k));
        }
    }
}
