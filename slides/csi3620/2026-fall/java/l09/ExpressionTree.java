package l09;

import java.util.ArrayList;
import java.util.List;

/**
 * Integer expression trees: operators at internal nodes, operands at leaves.
 * Precondition for evaluate and infix: the tree is nonempty and every operator node has
 * exactly two children. They throw IllegalArgumentException when it does not hold.
 * prefix and postfix are plain traversals and return "" for the empty tree.
 */
public final class ExpressionTree {

    private ExpressionTree() {
    }

    /** True for an operand; rejects null and one-child nodes, which break the precondition. */
    static boolean isLeaf(BinaryNode<String> x) {
        if (x == null) throw new IllegalArgumentException("empty expression");
        if ((x.left == null) != (x.right == null)) {
            throw new IllegalArgumentException("operator " + x.value + " needs two operands");
        }
        return x.left == null;
    }

    /** Postorder: evaluate both operands, then apply the operator. */
    static int evaluate(BinaryNode<String> x) {
        if (isLeaf(x)) return Integer.parseInt(x.value);   // an operand
        int a = evaluate(x.left);                 // left operand first,
        int b = evaluate(x.right);                // then the right one,
        return switch (x.value) {                 // then the operator
            case "+" -> a + b;
            case "-" -> a - b;
            case "*" -> a * b;
            case "/" -> a / b;
            default -> throw new IllegalArgumentException(
                    "unknown operator " + x.value);
        };
    }

    /** Inorder with parentheses around every operator. */
    static String infix(BinaryNode<String> x) {
        if (isLeaf(x)) return x.value;
        return "(" + infix(x.left) + " " + x.value + " " + infix(x.right) + ")";
    }

    static String prefix(BinaryNode<String> x) {
        List<String> out = new ArrayList<>();
        Traversals.preorder(x, out);
        return String.join(" ", out);
    }

    static String postfix(BinaryNode<String> x) {
        List<String> out = new ArrayList<>();
        Traversals.postorder(x, out);
        return String.join(" ", out);
    }
}
