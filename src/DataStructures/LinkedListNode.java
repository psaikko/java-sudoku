/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package DataStructures;

/**
 *
 * @author Paul
 */
public class LinkedListNode<T> {
    public LinkedListNode(T value) {
        this.value = value;
        next = null;
    }

    public LinkedListNode(T value, LinkedListNode next) {
        this.value = value;
        this.next = next;
    }

    public T value;
    public LinkedListNode<T> next;
}
