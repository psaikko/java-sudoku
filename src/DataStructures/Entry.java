/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package DataStructures;

/**
 *
 * @author Paul
 */
public class Entry<K, V> {
    
    private K key;
    private V value;
    
    public Entry(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() { return key; }
    public V getValue() { return value; }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Entry<K, V> other = (Entry<K, V>) obj;
        if (this.key != other.key && (this.key == null || !this.key.equals(other.key))) {
            return false;
        }
        if (this.value != other.value && (this.value == null || !this.value.equals(other.value))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 11 * hash + (this.key != null ? this.key.hashCode() : 0);
        hash = 11 * hash + (this.value != null ? this.value.hashCode() : 0);
        return hash;
    }
}
