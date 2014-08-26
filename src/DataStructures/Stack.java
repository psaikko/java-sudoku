/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package DataStructures;

import java.util.EmptyStackException;

/**
 *
 * @author Paul
 */
public class Stack<T> {
    private StackNode<T> top;
    private int size;

    public Stack() {
        top = null;
        size = 0;
    }

    public T push(T o) {
        StackNode newTop = new StackNode(o, top);
        top = newTop;
        size++;
        return o;
    }

    public T peek() {
        if (top == null)
            throw new EmptyStackException();
        return top.value;
    }

    public T pop() {
        if (top == null)
            throw new EmptyStackException();
        T val = top.value;
        top = top.prev;
        size--;
        return val;
    }

    public boolean empty() {
        return size == 0;
    }

    public int size() {
        return size;
    }
}
