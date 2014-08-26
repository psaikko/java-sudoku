/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package DataStructures;

import java.util.Iterator;

/**
 *
 * @author Paul
 */
public class LinkedListIterator<E> implements Iterator<E> {

    private LinkedListNode<E> next;
    private LinkedListNode<E> current;

    public LinkedListIterator(LinkedListNode<E> start) {
        current = null;
        next = start;
    }

    public boolean hasNext() {
        return next != null;
    }

    public E next() {
        current = next;
        next = next.next;
        return current.value;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}

