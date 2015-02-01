package info.debatty.java.graphs;

import java.io.Serializable;

/**
 *
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
        return "(" + id + " => " + value.toString() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (! o.getClass().isInstance(this)) {
            return false;
        }
        
        return this.id.equals(((Node) o).id);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }
    
}
