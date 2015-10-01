package info.debatty.java.graphs;

import java.io.Serializable;

/**
 * The nodes of a graph have an id (String) and a value (type T)
 * @author Thibault Debatty
 * @param <T> Type of value field
 */
public class Node<T> implements Serializable {
    public String id = "";
    public T value;
    
    public Node() {
    }
    
    public Node(String id) {
        this.id = id;
    }
    
    public Node(String id, T value) {
        this.id = id;
        this.value = value;
    }

    @Override
    public String toString() {
        
        String v = "";
        if (this.value != null) {
            v = this.value.toString();
        }
            
        return "(" + id + " => " + v + ")";
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        
        if (other == null) {
            return false;
        }
        
        if (! other.getClass().isInstance(this)) {
            return false;
        }
        
        return this.id.equals(((Node) other).id);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }
    
}
