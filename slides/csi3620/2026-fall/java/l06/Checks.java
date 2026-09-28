package l06;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/** Concrete checks for lecture 6. Prints "l06 OK" when every check passes. */
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

    static int sumWithIterator(SinglyLinkedList<Integer> list) {
        int total = 0;
        Iterator<Integer> it = list.iterator();
        while (it.hasNext()) {
            total += it.next();
        }
        return total;
    }

    static int sumWithForEach(SinglyLinkedList<Integer> list) {
        int total = 0;
        for (int value : list) { // the compiler calls iterator() for us
            total += value;
        }
        return total;
    }

    /** The running example of the lecture, one operation at a time. */
    private static void runningExample() {
        SinglyLinkedList<Integer> list = new SinglyLinkedList<>();
        list.addLast(12);
        checkEquals("[12]", list.toString(), "first addLast");
        list.addLast(7);
        checkEquals("[12, 7]", list.toString(), "second addLast");
        list.addFirst(4);
        checkEquals("[4, 12, 7]", list.toString(), "build");
        list.addLast(19);
        checkEquals("[4, 12, 7, 19]", list.toString(), "addLast");
        check(list.insertAfter(12, 25), "insertAfter finds 12");
        checkEquals("[4, 12, 25, 7, 19]", list.toString(), "insertAfter");
        checkEquals(5, list.size(), "size 5");
        checkEquals(25, list.get(2), "get(2)");
        checkEquals(3, list.indexOf(7), "indexOf 7");
        checkEquals(-1, list.indexOf(99), "indexOf missing");
        checkEquals(4, list.removeFirst(), "removeFirst");
        checkEquals("[12, 25, 7, 19]", list.toString(), "after removeFirst");
        check(list.remove(7), "remove 7");
        checkEquals("[12, 25, 19]", list.toString(), "after remove 7");
        checkEquals(19, list.getLast(), "tail still 19");
        list.reverse();
        checkEquals("[19, 25, 12]", list.toString(), "reverse");
        checkEquals(19, list.getFirst(), "head after reverse");
        checkEquals(12, list.getLast(), "tail after reverse");
        checkEquals(56, sumWithIterator(list), "iterator sum");
        checkEquals(56, sumWithForEach(list), "for-each sum");
        list.addLast(8);
        checkEquals("[19, 25, 12, 8]", list.toString(), "tail usable after reverse");
    }

    private static void singlyEdgeCases() {
        SinglyLinkedList<String> list = new SinglyLinkedList<>();
        check(list.isEmpty(), "new list empty");
        checkEquals("[]", list.toString(), "empty toString");
        checkThrows(NoSuchElementException.class, list::removeFirst, "removeFirst on empty");
        checkThrows(NoSuchElementException.class, list::getFirst, "getFirst on empty");
        checkThrows(IndexOutOfBoundsException.class, () -> list.get(0), "get on empty");
        check(!list.remove("x"), "remove on empty");
        check(!list.insertAfter("x", "y"), "insertAfter on empty");
        list.reverse();
        check(list.isEmpty(), "reverse of empty");

        list.addFirst("a");
        checkEquals("a", list.getLast(), "one element: tail set by addFirst");
        list.reverse();
        checkEquals("[a]", list.toString(), "reverse of one");
        checkEquals("a", list.removeFirst(), "remove only element");
        check(list.isEmpty(), "empty again");
        checkThrows(NoSuchElementException.class, list::getLast, "tail cleared");
        list.addLast("b");
        checkEquals("[b]", list.toString(), "addLast after emptying");

        // Removing the tail must move tail back; duplicates remove the first only.
        list.addLast("c");
        list.addLast("b");
        check(list.remove("b"), "remove first duplicate");
        checkEquals("[c, b]", list.toString(), "first duplicate gone");
        check(list.remove("b"), "remove tail");
        checkEquals("c", list.getLast(), "tail moved back");
        list.addLast("d");
        checkEquals("[c, d]", list.toString(), "append after tail removal");
        check(list.insertAfter("d", "e"), "insert after tail");
        checkEquals("e", list.getLast(), "tail follows insertAfter");
        checkEquals(3, list.size(), "size tracked");
        check(!list.remove("zz"), "remove missing");
        checkEquals(3, list.size(), "missing leaves size");

        // null values are allowed and found with Objects.equals.
        list.addFirst(null);
        checkEquals(0, list.indexOf(null), "find null");
        check(list.remove(null), "remove null");
        checkEquals("[c, d, e]", list.toString(), "null removed");
        checkThrows(IndexOutOfBoundsException.class, () -> list.get(3), "get past end");
        checkThrows(IndexOutOfBoundsException.class, () -> list.get(-1), "negative index");

        Iterator<String> it = list.iterator();
        it.next();
        it.next();
        it.next();
        check(!it.hasNext(), "iterator exhausted");
        checkThrows(NoSuchElementException.class, it::next, "next past end");
    }

    private static void doublyChecks() {
        DoublyLinkedList<Integer> list = new DoublyLinkedList<>();
        check(list.isEmpty(), "new doubly empty");
        checkEquals("[]", list.toString(), "empty doubly");
        checkThrows(NoSuchElementException.class, list::removeFirst, "removeFirst empty doubly");
        checkThrows(NoSuchElementException.class, list::removeLast, "removeLast empty doubly");

        DoublyLinkedList.Node<Integer> n12 = list.addLast(12);
        DoublyLinkedList.Node<Integer> n25 = list.addLast(25);
        list.addLast(19);
        checkEquals("[12, 25, 19]", list.toString(), "doubly build");
        checkEquals("[19, 25, 12]", list.toStringBackward(), "prev links agree");
        checkEquals(25, list.unlink(n25), "unlink middle");
        checkEquals("[12, 19]", list.toString(), "after unlink");
        checkEquals("[19, 12]", list.toStringBackward(), "backward after unlink");
        checkThrows(IllegalArgumentException.class, () -> list.unlink(n25), "unlink twice");
        checkEquals(12, list.unlink(n12), "unlink first");
        checkEquals(19, list.removeLast(), "removeLast");
        check(list.isEmpty(), "doubly empty again");
        checkEquals("[]", list.toStringBackward(), "empty backward");

        DoublyLinkedList<Integer> other = new DoublyLinkedList<>();
        DoublyLinkedList.Node<Integer> foreign = other.addFirst(5);
        checkThrows(IllegalArgumentException.class, () -> list.unlink(foreign), "foreign node");
        checkEquals(5, foreign.value(), "node value");

        list.addFirst(3);
        list.addFirst(2);
        list.addLast(4);
        check(list.find(9) == null, "find missing");
        check(list.remove(3), "remove by value");
        check(!list.remove(3), "remove missing");
        checkEquals("[2, 4]", list.toString(), "after remove by value");
        checkEquals(2, list.removeFirst(), "removeFirst doubly");
        checkEquals(1, list.size(), "doubly size");
        int total = 0;
        for (int v : list) {
            total += v;
        }
        checkEquals(4, total, "doubly iteration");
    }

    /** java.util.LinkedList supports both List and Deque operations. */
    private static void libraryChecks() {
        LinkedList<Integer> list = new LinkedList<>();
        list.addLast(12);
        list.addFirst(4);
        list.add(1, 25);
        checkEquals("[4, 25, 12]", list.toString(), "LinkedList add");
        checkEquals(25, list.get(1), "LinkedList get");
        checkEquals(12, list.removeLast(), "LinkedList removeLast");
        list.add(null);
        checkEquals(3, list.size(), "LinkedList permits null");
    }

    public static void main(String[] args) {
        runningExample();
        singlyEdgeCases();
        doublyChecks();
        libraryChecks();
        System.out.println("l06 OK");
    }
}
