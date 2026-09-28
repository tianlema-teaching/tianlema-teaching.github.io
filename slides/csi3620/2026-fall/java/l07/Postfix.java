package l07;

import java.util.ArrayDeque;
import java.util.Deque;

/** Evaluates integer postfix expressions such as "6 2 3 + * 4 -". */
public final class Postfix {

    private Postfix() {
    }

    public static int evaluate(String expression) {
        Deque<Integer> operands = new ArrayDeque<>();
        for (String token : expression.trim().split("\\s+")) {
            if (isOperator(token)) {
                if (operands.size() < 2) {
                    throw new IllegalArgumentException("missing operand for " + token);
                }
                int right = operands.pop(); // pushed last, so popped first
                int left = operands.pop();
                operands.push(apply(token, left, right));
            } else {
                operands.push(Integer.parseInt(token));
            }
            Trace.record(() -> token + " " + operands); // token, then stack top first
        }
        if (operands.size() != 1) {
            throw new IllegalArgumentException("expected one result, found " + operands.size());
        }
        return operands.pop();
    }

    private static boolean isOperator(String token) {
        return token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/");
    }

    private static int apply(String op, int left, int right) {
        return switch (op) {
            case "+" -> left + right;
            case "-" -> left - right;
            case "*" -> left * right;
            default -> left / right; // integer division; throws on zero
        };
    }
}
