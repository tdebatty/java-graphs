package info.debatty.java.graphs;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.HashMap;

/**
 * Neighbor of an edge (stores the other node, and the similarity)
 * @author Thibault Debatty
 */
public class Neighbor implements Comparable, Serializable {
    public Node node;
    public double similarity; 
    
    protected HashMap<String, Object> attributes;

    
    public Neighbor() {
        this.attributes = new HashMap<String, Object>();
        node = new Node();
    }
    
    public Neighbor(Node node, double similarity) {
        this.attributes = new HashMap<String, Object>();
        this.node = node;
        this.similarity = similarity;
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
     * Returns the value of this attribute, or null if this neighbor has no such
     * attribute
     * @param key
     * @return 
     */
    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    /**
     * 
     * @return (node.id,node.value,similarity)
     */
    @Override
    public String toString() {
        return "(" + node.id + "," + node.value + "," + similarity + ")";
    }


    @Override
    public boolean equals(Object other) {
        if (!other.getClass().getName().equals(this.getClass().getName())) {
            return false;
        }
        
        Neighbor other_neighbor = (Neighbor) other;
        return this.node.equals(other_neighbor.node);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (this.node != null ? this.node.hashCode() : 0);
        return hash;
    }
    
    @Override
    public int compareTo(Object other) {
        if (! other.getClass().isInstance(this)) {
            throw new InvalidParameterException();
        }
        
        if (((Neighbor) other).node.equals(this.node)) {
            return 0;
        }
        
        if (this.similarity == ((Neighbor)other).similarity) {
            return 0;
        }
        
        return this.similarity > ((Neighbor)other).similarity ? 1 : -1;
    }    
}
