package l07;

import java.util.NoSuchElementException;

/** A stack on a singly linked list; the top is the first node. */
public class LinkedStack<T> implements SimpleStack<T> {

    private static class Node<T> {
        final T value;
        final Node<T> next;

        Node(T value, Node<T> next) {
            this.value = value;
            this.next = next;
        }
    }

    private Node<T> top; // null when empty
    private int size;

    @Override
    public void push(T value) {
        top = new Node<>(value, top);
        size++;
    }

    @Override
    public T pop() {
        if (top == null) {
            throw new NoSuchElementException("stack is empty");
        }
        T value = top.value;
        top = top.next;
        size--;
        return value;
    }

    @Override
    public T peek() {
        if (top == null) {
            throw new NoSuchElementException("stack is empty");
        }
        return top.value;
    }

    @Override
    public boolean isEmpty() {
        return top == null;
    }

    @Override
    public int size() {
        return size;
    }
}
