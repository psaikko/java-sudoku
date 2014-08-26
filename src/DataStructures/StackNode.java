/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package DataStructures;

/**
 *
 * @author Paul
 */
public class StackNode<T> {
    T value;
    StackNode prev;
    
    public StackNode(T value) {
        this.value = value;
    }

    public StackNode(T value, StackNode prev) {
        this.value = value;
        this.prev = prev;
    }

}
