package com.sci.bfc.util;

import java.util.NoSuchElementException;

public final class Stack<T> {
    private class Node {
        public final Node prev;
        public final T value;

        private Node(final Node prev, final T value) {
            this.prev = prev;
            this.value = value;
        }
    }

    private Node head;

    public void push(final T value) {
        this.head = new Node(head, value);
    }

    public T peek() {
        if(this.head == null) throw new NoSuchElementException("Attempt to peek empty stack");
        return this.head.value;
    }

    public T pop() {
        final T value = this.peek();
        this.head = this.head.prev;
        return value;
    }
}