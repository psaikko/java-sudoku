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
public class HashMap<K, V> {

    protected LinkedList<Entry<K, V>>[] hashmap;
    private int size;

    public HashMap() {
        hashmap = new LinkedList[10];
        for (int i = 0; i < hashmap.length; i++)
            hashmap[i] = new LinkedList<Entry<K,V>>();
        size = 0;
    }

    public boolean removeKey(K key) {
        for (int i = 0; i < hashmap.length; i++) {
            Entry<K,V> toRemove = null;
            for (Entry<K,V> e : hashmap[i])
                if (e.getKey().equals(key)) {
                    toRemove = e;
                    break;
                }

            if (toRemove != null) {
                hashmap[i].remove(toRemove);
                return true;
            }
        }
        return false;
    }

    public boolean containsKey(K key) {
        for (Entry<K,V> entry : hashmap[hash(key, hashmap.length)])
            if (entry.getKey().equals(key))
                return true;
        return false;
    }

    public Collection<Entry<K,V>> entrySet() {
        LinkedList<Entry<K,V>> entrySet = new LinkedList<Entry<K,V>>();
        for (int i = 0; i < hashmap.length; i++)
            for (Entry<K, V> entry : hashmap[i])
                entrySet.add(entry);
        return entrySet;
    }

    public Collection<K> keySet() {
        LinkedList<K> keySet = new LinkedList<K>();
        for (int i = 0; i < hashmap.length; i++)
            for (Entry<K, V> entry : hashmap[i])
                keySet.add(entry.getKey());
        return keySet;
    }

    public V put(K key, V value) {
        Entry<K,V> old = null;
        V prev = null;
        for (Entry<K, V> entry : hashmap[hash(key, hashmap.length)])
            if (entry.getKey().equals(key)) {
                old = entry;
                prev = entry.getValue();
            }
        if (old != null) {
            hashmap[hash(key, hashmap.length)].remove(old);
            size--;
        }

        hashmap[hash(key, hashmap.length)].add(new Entry<K,V>(key, value));

        size++;

        if (size > hashmap.length * 3)
            expand();

        return prev;
    }

    public V get(K key) {
        for (Entry<K, V> entry : hashmap[hash(key, hashmap.length)])
            if (entry.getKey().equals(key))
                return entry.getValue();
        return null;
    }

    private void expand() {
        LinkedList<Entry<K, V>>[] newMap = new LinkedList[hashmap.length * 2];
        for (int i = 0; i < newMap.length; i++)
            newMap[i] = new LinkedList<Entry<K, V>>();
        for (int i = 0; i < hashmap.length; i++)
            for (Entry<K, V> entry : hashmap[i])
                newMap[hash(entry.getKey(), newMap.length)].add(entry);
        hashmap = newMap;
    }

    private int hash(K key, int length) {
        return Math.abs(key.hashCode() % length);
    }

    public int size() { return size; }

}
