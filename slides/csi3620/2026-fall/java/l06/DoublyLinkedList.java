package l06;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A doubly linked list built around one sentinel node. The sentinel's next is
 * the first node and its prev is the last, so the links form a circle and no
 * link is ever null while a node is in the list.
 */
public class DoublyLinkedList<T> implements Iterable<T> {

    /** A handle to one element; callers keep it to remove in O(1). */
    public static final class Node<T> {
        private final T value;
        private Node<T> prev;
        private Node<T> next;
        private DoublyLinkedList<T> owner;

        private Node(T value, DoublyLinkedList<T> owner) {
            this.value = value;
            this.owner = owner;
        }

        public T value() {
            return value;
        }
    }

    private final Node<T> sentinel = new Node<>(null, null);
    private int size;

    public DoublyLinkedList() {
        sentinel.prev = sentinel;
        sentinel.next = sentinel;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    private Node<T> linkAfter(Node<T> before, T value) {
        Node<T> node = new Node<>(value, this);
        Node<T> after = before.next;
        node.prev = before;
        node.next = after;
        before.next = node;
        after.prev = node;
        size++;
        return node;
    }

    public Node<T> addFirst(T value) {
        return linkAfter(sentinel, value);
    }

    public Node<T> addLast(T value) {
        return linkAfter(sentinel.prev, value);
    }

    /** Removes a node of this list in O(1): no search, no special cases. */
    public T unlink(Node<T> node) {
        if (node.owner != this) {
            throw new IllegalArgumentException("node is not in this list");
        }
        node.prev.next = node.next;
        node.next.prev = node.prev;
        node.prev = null;
        node.next = null;
        node.owner = null;
        size--;
        return node.value;
    }

    public T removeFirst() {
        if (size == 0) {
            throw new NoSuchElementException("list is empty");
        }
        return unlink(sentinel.next);
    }

    public T removeLast() {
        if (size == 0) {
            throw new NoSuchElementException("list is empty");
        }
        return unlink(sentinel.prev);
    }

    /** Returns the first node equal to value, or null. O(n). */
    public Node<T> find(T value) {
        for (Node<T> n = sentinel.next; n != sentinel; n = n.next) {
            if (Objects.equals(n.value, value)) {
                return n;
            }
        }
        return null;
    }

    public boolean remove(T value) {
        Node<T> node = find(value);
        if (node == null) {
            return false;
        }
        unlink(node);
        return true;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private Node<T> current = sentinel.next;

            @Override
            public boolean hasNext() {
                return current != sentinel;
            }

            @Override
            public T next() {
                if (current == sentinel) {
                    throw new NoSuchElementException();
                }
                T value = current.value;
                current = current.next;
                return value;
            }
        };
    }

    /** Values from last to first, following prev links. */
    public String toStringBackward() {
        StringBuilder sb = new StringBuilder("[");
        for (Node<T> n = sentinel.prev; n != sentinel; n = n.prev) {
            sb.append(n.value);
            if (n.prev != sentinel) {
                sb.append(", ");
            }
        }
        return sb.append("]").toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (Node<T> n = sentinel.next; n != sentinel; n = n.next) {
            sb.append(n.value);
            if (n.next != sentinel) {
                sb.append(", ");
            }
        }
        return sb.append("]").toString();
    }
}
