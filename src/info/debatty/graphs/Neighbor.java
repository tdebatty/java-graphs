package info.debatty.graphs;

import java.security.InvalidParameterException;

/**
 *
 * @author tibo
 */
public class Neighbor implements Comparable {
    public Node node;
    public double similarity;
    
    public boolean is_new = true; // only used by sequential nndescent...

    public static final String DELIMITER = ",,,";
    
    public Neighbor() {
        node = new Node();
    }
    
    public Neighbor(Node node, double similarity) {
        this.node = node;
        this.similarity = similarity;
    }

    @Override
    public String toString() {
        return node.toString() + DELIMITER + String.valueOf(similarity);
    }
    
    public static Neighbor parseString(String s) {
        String[] values = s.split(DELIMITER, 2);
        return new Neighbor(Node.parseString(values[0]), Double.valueOf(values[1]));
        
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
        
        if (this.similarity == ((Neighbor)other).similarity) {
            return 0;
        }
        
        return this.similarity > ((Neighbor)other).similarity ? 1 : -1;
    }    
}
