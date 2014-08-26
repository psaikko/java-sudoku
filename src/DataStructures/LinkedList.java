/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package DataStructures;

import java.util.Collection;
/**
 *
 * @author Paul
 */
public class LinkedList<T> implements Collection<T> {

    private LinkedListNode<T> head;
    private LinkedListNode<T> tail;
    private int size;

    public LinkedList() {
        head = null;
        tail = null;
        size = 0;
    }

    public T getFirst() {
        if (head == null)
            return null;
        return head.value;
    }

    public T getLast() {
        if (tail == null)
            return null;
        return tail.value;
    }

    public T get(int i) {
        if (i > size - 1)
            throw new IndexOutOfBoundsException();
        LinkedListNode<T> temp = head;
        int j = 0;
        while (j < i) {
            temp = temp.next;
            j++;
        }
        return temp.value;
    }

    public LinkedListIterator<T> iterator() {
        return new LinkedListIterator<T>(head);
    }

    public boolean add(T o) {
        LinkedListNode<T> newNode = new LinkedListNode<T>(o, null);
        if (head == null) {
            head = newNode;
            tail = head;
        } else {
            tail.next = newNode;
            tail = newNode;
        }
        size++;
        return true;
    }

    public boolean addAll(Collection<? extends T> c) {
        boolean changed = false;
        for (T t : c) {
            this.add(t);
            size++;
            changed = true;
        }
        return changed;
    }

    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }

    public boolean contains(Object o) {
        LinkedListNode<T> temp = head;
        while (temp != null) {
            if (temp.value.equals(o))
                return true;
            temp = temp.next;
        }
        return false;
    }

    public boolean containsAll(Collection<?> c) {
        for (Object o : c)
            if (!this.contains(o))
                return false;
        return true;
    }

    public boolean remove(Object o) {
        LinkedListNode<T> temp = head;
        LinkedListNode<T> prev = null;
        while (temp != null) {
            if (temp.value.equals(o)) {
                if (temp == head)
                    head = temp.next;
                else
                    prev.next = temp.next;
                if (temp == tail)
                    tail = prev;
                size--;
                return true;
            }
            prev = temp;
            temp = temp.next;
        }
        return false;
    }

    public T removeFirst() {
        T value = head.value;
        head = head.next;
        size--;
        return value;
    }


    public boolean removeAll(Collection<?> c) {
        boolean changed = false;
        for (Object o : c)
            if (this.remove(o) == true)
                changed = true;
        return changed;
    }

    public boolean retainAll(Collection<?> c) {
        LinkedListNode<T> temp = head;
        LinkedListNode<T> prev = null;
        boolean changed = false;
        while (temp != null) {
            if (!c.contains(temp.value)) {
                if (prev != null)
                    prev.next = temp.next;
                else
                    head = temp.next;
                temp = temp.next;
                changed = true;
                size--;
            } else {
                prev = temp;
                temp = temp.next;
            }
        }
        return changed;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public Object[] toArray() {
        Object[] array = new Object[size];
        LinkedListIterator<T> iter = this.iterator();
        int i = 0;
        while(iter.hasNext())
            array[i++] = iter.next();
        return array;
    }

    // from
    // http://stackoverflow.com/questions/4010924/java-how-to-implement-toarray-for-collection
    public <T> T[] toArray(T[] array) {
        if (array.length < size)
            array = (T[])java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), size);
        else if (array.length > size)
            array[size] = null;

        int i = 0;
        LinkedListNode temp = head;
        while (temp != null) {
            array[i++] = (T)temp.value;
            temp = temp.next;
        }

        return array;
    }
}
