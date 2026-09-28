package l06;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/** A singly linked list with head and tail references and a size count. */
public class SinglyLinkedList<T> implements Iterable<T> {

    private static class Node<T> {
        T value;
        Node<T> next;

        Node(T value, Node<T> next) {
            this.value = value;
            this.next = next;
        }
    }

    private Node<T> head; // first node, or null when the list is empty
    private Node<T> tail; // last node, or null when the list is empty
    private int size;     // number of nodes reachable from head

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void addFirst(T value) {
        head = new Node<>(value, head);
        if (tail == null) {
            tail = head;
        }
        size++;
    }

    public void addLast(T value) {
        Node<T> node = new Node<>(value, null);
        if (tail == null) {
            head = node;
        } else {
            tail.next = node;
        }
        tail = node;
        size++;
    }

    public T getFirst() {
        if (head == null) {
            throw new NoSuchElementException("list is empty");
        }
        return head.value;
    }

    public T getLast() {
        if (tail == null) {
            throw new NoSuchElementException("list is empty");
        }
        return tail.value;
    }

    public T get(int index) {
        Objects.checkIndex(index, size);
        Node<T> current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current.value;
    }

    /** Returns the index of the first node equal to value, or -1. */
    public int indexOf(T value) {
        int i = 0;
        for (Node<T> current = head; current != null; current = current.next) {
            if (Objects.equals(current.value, value)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public boolean contains(T value) {
        return indexOf(value) >= 0;
    }

    /** Inserts value right after the first node equal to target. */
    public boolean insertAfter(T target, T value) {
        Node<T> current = head;
        while (current != null && !Objects.equals(current.value, target)) {
            current = current.next;
        }
        if (current == null) {
            return false;
        }
        current.next = new Node<>(value, current.next);
        if (current == tail) {
            tail = current.next;
        }
        size++;
        return true;
    }

    public T removeFirst() {
        if (head == null) {
            throw new NoSuchElementException("list is empty");
        }
        T value = head.value;
        head = head.next;
        if (head == null) {
            tail = null;
        }
        size--;
        return value;
    }

    /** Removes the first node equal to value; returns false if none. */
    public boolean remove(T value) {
        if (head == null) {
            return false;
        }
        if (Objects.equals(head.value, value)) {
            removeFirst();
            return true;
        }
        Node<T> prev = head;
        while (prev.next != null && !Objects.equals(prev.next.value, value)) {
            prev = prev.next;
        }
        if (prev.next == null) {
            return false;
        }
        if (prev.next == tail) {
            tail = prev;
        }
        prev.next = prev.next.next;
        size--;
        return true;
    }

    /** Reverses the links in place with three references. */
    public void reverse() {
        tail = head;
        Node<T> prev = null;
        Node<T> current = head;
        while (current != null) {
            Node<T> next = current.next; // save the rest of the list
            current.next = prev;         // flip one link
            prev = current;              // advance both references
            current = next;
        }
        head = prev;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private Node<T> current = head;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public T next() {
                if (current == null) {
                    throw new NoSuchElementException();
                }
                T value = current.value;
                current = current.next;
                return value;
            }
        };
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (Node<T> current = head; current != null; current = current.next) {
            sb.append(current.value);
            if (current.next != null) {
                sb.append(", ");
            }
        }
        return sb.append("]").toString();
    }
}
