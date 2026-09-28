package l07;

import java.util.NoSuchElementException;

/** A queue on a singly linked list: dequeue at head, enqueue at tail. */
public class LinkedQueue<T> implements SimpleQueue<T> {

    private static class Node<T> {
        final T value;
        Node<T> next;

        Node(T value) {
            this.value = value;
        }
    }

    private Node<T> head; // front: oldest element, null when empty
    private Node<T> tail; // back: newest element, null when empty
    private int size;

    @Override
    public void enqueue(T value) {
        Node<T> node = new Node<>(value);
        if (tail == null) {
            head = node;
        } else {
            tail.next = node;
        }
        tail = node;
        size++;
    }

    @Override
    public T dequeue() {
        if (head == null) {
            throw new NoSuchElementException("queue is empty");
        }
        T value = head.value;
        head = head.next;
        if (head == null) {
            tail = null; // the queue became empty
        }
        size--;
        return value;
    }

    @Override
    public T peek() {
        if (head == null) {
            throw new NoSuchElementException("queue is empty");
        }
        return head.value;
    }

    @Override
    public boolean isEmpty() {
        return head == null;
    }

    @Override
    public int size() {
        return size;
    }
}
