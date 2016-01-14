package info.debatty.java.graphs;

import java.io.Serializable;
import java.util.HashMap;

/**
 * The nodes of a graph have an id (String) and a value (type T)
 * @author Thibault Debatty
 * @param <T> Type of value field
 */
public class Node<T> implements Serializable {
    public String id = "";
    public T value;
    
    private final HashMap<String, Object> attributes;
    
    public Node() {
        this.attributes = new HashMap<String, Object>(0);
    }
    
    public Node(String id) {
        this.attributes = new HashMap<String, Object>(0);
        this.id = id;
    }
    
    public Node(String id, T value) {
        this.attributes = new HashMap<String, Object>(0);
        this.id = id;
        this.value = value;
    }
    
    /**
     * 
     * @param key
     * @param value 
     */
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }
    
    /**
     * 
     * @param key
     * @return 
     */
    public Object getAttribute(String key) {
        return attributes.get(key);
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
