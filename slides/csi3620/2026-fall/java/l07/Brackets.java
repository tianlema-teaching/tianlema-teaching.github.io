package l07;

import java.util.ArrayDeque;
import java.util.Deque;

/** Checks that (), [] and {} are balanced and properly nested. */
public final class Brackets {

    private Brackets() {
    }

    public static boolean isBalanced(String text) {
        Deque<Character> open = new ArrayDeque<>();
        for (char c : text.toCharArray()) {
            if (c == '(' || c == '[' || c == '{') {
                open.push(c);
            } else if (c == ')' || c == ']' || c == '}') {
                if (open.isEmpty() || open.pop() != opener(c)) {
                    return false; // nothing to match, or the wrong kind
                }
            }
            Trace.record(() -> c + " " + open); // character, then stack top first
        }
        return open.isEmpty(); // leftovers were never closed
    }

    private static char opener(char closer) {
        return switch (closer) {
            case ')' -> '(';
            case ']' -> '[';
            default -> '{';
        };
    }
}
