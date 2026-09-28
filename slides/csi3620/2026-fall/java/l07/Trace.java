package l07;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Records the intermediate states shown on the lecture's step-by-step slides. */
public final class Trace {
    private static List<String> states; // null when nothing is being captured

    private Trace() {
    }

    /** Called from the algorithms' loops; builds the state only while a capture is running. */
    static void record(Supplier<String> state) {
        if (states != null) {
            states.add(state.get());
        }
    }

    /** Runs the action and returns every state it recorded, in order. */
    static List<String> capture(Runnable action) {
        List<String> previous = states;
        List<String> captured = new ArrayList<>();
        states = captured;
        try {
            action.run();
        } finally {
            states = previous;
        }
        return captured;
    }

    static List<String> brackets(String text) {
        return capture(() -> Brackets.isBalanced(text));
    }

    static List<String> postfix(String expression) {
        return capture(() -> Postfix.evaluate(expression));
    }

    static List<String> bfs(List<List<Integer>> adjacency, int start) {
        return capture(() -> BfsPreview.order(adjacency, start));
    }

    /** The graph traced on the BFS slides: edges 0-1, 0-2, 1-3, 2-3, 2-4. */
    static List<List<Integer>> slideGraph() {
        return List.of(List.of(1, 2), List.of(0, 3), List.of(0, 3, 4), List.of(1, 2), List.of(2));
    }

    /** Prints the states used on the slides; stacks print top first, queues front first. */
    public static void main(String[] args) {
        for (String text : new String[] {"{a[(b)c]}", "{[(])}", "(()"}) {
            System.out.println("brackets " + text + " -> " + Brackets.isBalanced(text));
            brackets(text).forEach(s -> System.out.println("  " + s));
        }
        String expression = "6 2 3 + * 4 -";
        System.out.println("postfix " + expression + " -> " + Postfix.evaluate(expression));
        postfix(expression).forEach(s -> System.out.println("  " + s));
        System.out.println("bfs from 0 -> " + BfsPreview.order(slideGraph(), 0));
        bfs(slideGraph(), 0).forEach(s -> System.out.println("  " + s));
    }
}
