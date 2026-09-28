package l07;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;

/** Concrete checks for lecture 7. Prints "l07 OK" when every check passes. */
public final class Checks {

    private Checks() {
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void checkEquals(Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(message + ": expected " + expected + " but got " + actual);
        }
    }

    private static void checkThrows(Class<? extends Throwable> type, Runnable action, String message) {
        try {
            action.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) {
                return;
            }
            throw new AssertionError(message + ": wrong exception " + t);
        }
        throw new AssertionError(message + ": nothing thrown");
    }

    /** Push 10, 20, 30; pop; push 40: the lecture's running stack example. */
    private static void stackContract(SimpleStack<Integer> stack, String name) {
        check(stack.isEmpty(), name + " starts empty");
        checkThrows(NoSuchElementException.class, stack::pop, name + " pop on empty");
        checkThrows(NoSuchElementException.class, stack::peek, name + " peek on empty");
        stack.push(10);
        stack.push(20);
        stack.push(30);
        checkEquals(30, stack.peek(), name + " peek sees top");
        checkEquals(3, stack.size(), name + " size 3");
        checkEquals(30, stack.pop(), name + " pop last in");
        stack.push(40);
        checkEquals(40, stack.pop(), name + " pop 40");
        checkEquals(20, stack.pop(), name + " pop 20");
        checkEquals(10, stack.pop(), name + " pop 10");
        check(stack.isEmpty(), name + " empty at end");
        for (int i = 0; i < 1000; i++) {
            stack.push(i);
        }
        for (int i = 999; i >= 0; i--) {
            checkEquals(i, stack.pop(), name + " LIFO order at scale");
        }
    }

    /** Enqueue 10, 20, 30; dequeue; enqueue 40: the running queue example. */
    private static void queueContract(SimpleQueue<Integer> queue, String name) {
        check(queue.isEmpty(), name + " starts empty");
        checkThrows(NoSuchElementException.class, queue::dequeue, name + " dequeue on empty");
        checkThrows(NoSuchElementException.class, queue::peek, name + " peek on empty");
        queue.enqueue(10);
        queue.enqueue(20);
        queue.enqueue(30);
        checkEquals(10, queue.peek(), name + " peek sees front");
        checkEquals(10, queue.dequeue(), name + " first in, first out");
        queue.enqueue(40);
        checkEquals(20, queue.dequeue(), name + " dequeue 20");
        checkEquals(30, queue.dequeue(), name + " dequeue 30");
        checkEquals(40, queue.dequeue(), name + " dequeue 40");
        check(queue.isEmpty(), name + " empty again");
        queue.enqueue(50);
        checkEquals(50, queue.peek(), name + " usable after emptying");
        checkEquals(50, queue.dequeue(), name + " dequeue 50");
        for (int i = 0; i < 1000; i++) {
            queue.enqueue(i);
            if (i % 3 == 0) {
                queue.enqueue(-i);
                queue.dequeue();
            }
        }
        checkEquals(1000, queue.size(), name + " size after mixed use");
    }

    private static void arrayStackChecks() {
        ArrayStack<Integer> stack = new ArrayStack<>(2);
        stack.push(10);
        stack.push(20);
        checkEquals("[10, 20]", stack.slotsView(), "full at capacity 2");
        stack.push(30);
        checkEquals("[10, 20, 30, _]", stack.slotsView(), "doubled to 4");
        checkEquals(2L, stack.copiesSoFar(), "two copies so far");
        checkEquals(30, stack.pop(), "pop 30");
        checkEquals("[10, 20, _, _]", stack.slotsView(), "slot cleared");
        stack.push(40);
        checkEquals("[10, 20, 40, _]", stack.slotsView(), "push 40 reuses slot");

        // Doubling argument: n pushes from capacity 1 copy fewer than 2n elements.
        for (int n : new int[] {1, 2, 3, 5, 8, 100, 1025, 5000}) {
            ArrayStack<Integer> s = new ArrayStack<>(1);
            for (int i = 0; i < n; i++) {
                s.push(i);
            }
            check(s.copiesSoFar() < 2L * n, "copies < 2n for n = " + n);
            check(s.capacity() < 2 * n || n == 1, "capacity < 2n for n = " + n);
        }
        ArrayStack<Integer> s = new ArrayStack<>(1);
        for (int i = 0; i < 1025; i++) {
            s.push(i);
        }
        checkEquals(2047L, s.copiesSoFar(), "1 + 2 + ... + 1024 copies");
        checkThrows(IllegalArgumentException.class, () -> new ArrayStack<Integer>(0), "capacity 0");
    }

    private static void circularQueueChecks() {
        CircularArrayQueue<Integer> q = new CircularArrayQueue<>(4);
        q.enqueue(10);
        q.enqueue(20);
        q.enqueue(30);
        checkEquals("[10, 20, 30, _] front=0 size=3", q.slotsView(), "three in");
        checkEquals(10, q.dequeue(), "dequeue 10");
        checkEquals(20, q.dequeue(), "dequeue 20");
        checkEquals("[_, _, 30, _] front=2 size=1", q.slotsView(), "front moved");
        q.enqueue(40);
        q.enqueue(50);
        checkEquals("[50, _, 30, 40] front=2 size=3", q.slotsView(), "50 wrapped to 0");
        q.enqueue(60);
        checkEquals("[50, 60, 30, 40] front=2 size=4", q.slotsView(), "full");
        q.enqueue(70);
        checkEquals("[30, 40, 50, 60, 70, _, _, _] front=0 size=5", q.slotsView(), "grown in order");
        checkEquals(30, q.peek(), "peek after grow");
        checkEquals(30, q.poll(), "poll");
        CircularArrayQueue<Integer> eight = new CircularArrayQueue<>(8);
        for (int i = 0; i < 6; i++) {
            eight.enqueue(i);
            eight.dequeue();
        }
        eight.enqueue(1);
        eight.enqueue(2);
        eight.enqueue(3);
        checkEquals("[3, _, _, _, _, _, 1, 2] front=6 size=3", eight.slotsView(), "front 6, size 3");
        eight.enqueue(4);
        checkEquals("[3, 4, _, _, _, _, 1, 2] front=6 size=4", eight.slotsView(), "next goes to slot 1");
        CircularArrayQueue<Integer> empty = new CircularArrayQueue<>(1);
        check(empty.poll() == null, "poll on empty returns null");
        checkThrows(NoSuchElementException.class, empty::dequeue, "dequeue on empty throws");
        checkThrows(IllegalArgumentException.class, () -> new CircularArrayQueue<Integer>(0), "capacity 0");
        CircularArrayQueue<Integer> noNull = new CircularArrayQueue<>(2);
        checkThrows(NullPointerException.class, () -> noNull.enqueue(null), "enqueue(null) rejected");
        check(noNull.isEmpty(), "rejected null was not stored");
    }

    /** The states drawn on the step-by-step slides, recorded by the algorithms' own loops. */
    private static void traceChecks() {
        checkEquals(List.of("{ [{]", "a [{]", "[ [[, {]", "( [(, [, {]", "b [(, [, {]", ") [[, {]",
                "c [[, {]", "] [{]", "} []"), Trace.brackets("{a[(b)c]}"), "bracket trace slide");
        checkEquals(List.of("{ [{]", "[ [[, {]", "( [(, [, {]"), Trace.brackets("{[(])}"),
                "crossed trace stops at the mismatched ]");
        checkEquals(List.of("( [(]", "( [(, (]", ") [(]"), Trace.brackets("(()"), "unclosed trace");
        checkEquals(List.of("6 [6]", "2 [2, 6]", "3 [3, 2, 6]", "+ [5, 6]", "* [30]", "4 [4, 30]",
                "- [26]"), Trace.postfix("6 2 3 + * 4 -"), "postfix trace slide");
        checkEquals(List.of("visit 0, queue [1, 2]", "visit 1, queue [2, 3]", "visit 2, queue [3, 4]",
                "visit 3, queue [4]", "visit 4, queue []"), Trace.bfs(Trace.slideGraph(), 0), "bfs trace slide");
        Brackets.isBalanced("()");
        checkEquals(List.of(), Trace.capture(() -> { }), "nothing recorded outside a capture");
        // Outside a capture the state is never built, so traced loops keep their normal cost.
        Trace.record(() -> { throw new AssertionError("trace state built outside a capture"); });
        String deep = "(".repeat(50_000) + ")".repeat(50_000);
        check(Brackets.isBalanced(deep), "deep nesting balanced");
    }

    private static void applicationChecks() {
        check(Brackets.isBalanced("{a[(b)c]}"), "nested");
        check(Brackets.isBalanced(""), "empty text");
        check(Brackets.isBalanced("()[]{}"), "sequence");
        check(!Brackets.isBalanced("{[(])}"), "crossed");
        check(!Brackets.isBalanced("(()"), "unclosed");
        check(!Brackets.isBalanced("())"), "extra closer");
        check(!Brackets.isBalanced("]"), "closer first");

        checkEquals(26, Postfix.evaluate("6 2 3 + * 4 -"), "running postfix");
        checkEquals(-1, Postfix.evaluate("3 4 -"), "left minus right");
        checkEquals(18, Postfix.evaluate("8 2 - 3 *"), "quiz postfix");
        checkEquals(2, Postfix.evaluate("7 3 /"), "integer division");
        checkEquals(42, Postfix.evaluate("  42 "), "single number");
        checkThrows(IllegalArgumentException.class, () -> Postfix.evaluate("2 +"), "missing operand");
        checkThrows(IllegalArgumentException.class, () -> Postfix.evaluate("2 3"), "leftover operand");
        checkThrows(NumberFormatException.class, () -> Postfix.evaluate("2 x +"), "bad token");
        checkThrows(ArithmeticException.class, () -> Postfix.evaluate("1 0 /"), "divide by zero");

        List<String> log = new ArrayList<>();
        checkEquals(6, CallStackDemo.sumTo(3, log), "sumTo(3)");
        checkEquals(List.of("call sumTo(3)", "call sumTo(2)", "call sumTo(1)", "call sumTo(0)",
                "return 0 from sumTo(0)", "return 1 from sumTo(1)", "return 3 from sumTo(2)",
                "return 6 from sumTo(3)"), log, "calls return in reverse order");

        List<List<Integer>> graph = List.of(
                List.of(1, 2), List.of(0, 3), List.of(0, 3, 4), List.of(1, 2), List.of(2));
        checkEquals(List.of(0, 1, 2, 3, 4), BfsPreview.order(graph, 0), "bfs from 0");
        checkEquals(List.of(4, 2, 0, 3, 1), BfsPreview.order(graph, 4), "bfs from 4");
    }

    /** Library behaviour the lecture relies on. */
    private static void libraryChecks() {
        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(10);
        stack.push(20);
        checkEquals(20, stack.peek(), "ArrayDeque push/peek");
        checkEquals(20, stack.pop(), "ArrayDeque pop");
        checkEquals(10, stack.pop(), "ArrayDeque pop 10");
        check(stack.peek() == null, "Deque.peek on empty is null");
        check(stack.poll() == null, "Deque.poll on empty is null");
        checkThrows(NoSuchElementException.class, stack::pop, "Deque.pop on empty throws");
        checkThrows(NoSuchElementException.class, stack::remove, "Deque.remove on empty throws");
        checkThrows(NoSuchElementException.class, stack::element, "Deque.element on empty throws");
        checkThrows(NullPointerException.class, () -> stack.push(null), "ArrayDeque rejects null");
        checkThrows(NoSuchElementException.class, stack::getFirst, "Deque.getFirst on empty throws");
        check(stack.peekFirst() == null, "Deque.peekFirst on empty is null");
        check(stack.pollFirst() == null, "Deque.pollFirst on empty is null");
        check(stack.offerFirst(30), "offerFirst succeeds on an unbounded deque");
        checkEquals(30, stack.peekFirst(), "offerFirst adds at the top");
        checkEquals(30, stack.pollFirst(), "pollFirst removes the top");

        Queue<Integer> queue = new ArrayDeque<>();
        check(queue.offer(10), "offer succeeds");
        check(queue.add(20), "add succeeds");
        checkEquals(10, queue.poll(), "poll front");
        checkEquals(20, queue.remove(), "remove front");

        java.util.Stack<Integer> legacy = new java.util.Stack<>();
        legacy.push(1);
        checkEquals(1, legacy.pop(), "legacy Stack works");
        checkThrows(java.util.EmptyStackException.class, legacy::pop, "legacy Stack throws");
        legacy.push(1);
        legacy.push(2);
        checkEquals("[1, 2]", legacy.toString(), "Stack iterates bottom first");
        checkEquals(1, legacy.get(0), "Stack allows access by index");
        Deque<Integer> modern = new ArrayDeque<>();
        modern.push(1);
        modern.push(2);
        checkEquals("[2, 1]", modern.toString(), "ArrayDeque iterates top first");
    }

    public static void main(String[] args) {
        stackContract(new ArrayStack<>(), "ArrayStack");
        stackContract(new ArrayStack<>(1), "ArrayStack(1)");
        stackContract(new LinkedStack<>(), "LinkedStack");
        queueContract(new LinkedQueue<>(), "LinkedQueue");
        queueContract(new CircularArrayQueue<>(1), "CircularArrayQueue(1)");
        queueContract(new CircularArrayQueue<>(4), "CircularArrayQueue(4)");
        arrayStackChecks();
        circularQueueChecks();
        applicationChecks();
        traceChecks();
        libraryChecks();
        System.out.println("l07 OK");
    }
}
