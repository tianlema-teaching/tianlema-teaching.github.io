package l09;

/** A node of a binary tree; null children mean "missing". */
public final class BinaryNode<T> {
    T value;
    BinaryNode<T> left, right;

    BinaryNode(T value) {
        this.value = value;
    }

    BinaryNode(T value, BinaryNode<T> left, BinaryNode<T> right) {
        this.value = value;
        this.left = left;
        this.right = right;
    }

    static <T> BinaryNode<T> leaf(T value) {
        return new BinaryNode<>(value);
    }

    /** The running tree: the BST built in Lecture 8 from 50, 30, 70, 20, 40, 80, 35, 45. */
    static BinaryNode<Integer> running() {
        return new BinaryNode<>(50,
                new BinaryNode<>(30, leaf(20), new BinaryNode<>(40, leaf(35), leaf(45))),
                new BinaryNode<>(70, null, leaf(80)));
    }

    /** The expression tree for (3 + 4) * (5 - 2). */
    static BinaryNode<String> expression() {
        return new BinaryNode<>("*",
                new BinaryNode<>("+", leaf("3"), leaf("4")),
                new BinaryNode<>("-", leaf("5"), leaf("2")));
    }
}
