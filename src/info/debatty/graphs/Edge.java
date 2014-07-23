package info.debatty.graphs;

import java.security.InvalidParameterException;

/**
 *
 * @author tibo
 */
public class Edge implements Comparable {
    public Node n1;
    public Node n2;
    public double similarity = 0;
    
    public static final String SEPARATOR = ";";
    
    public Edge() {
        
    }

    public Edge(Node n1, Node n2, double similarity) {
        this.n1 = n1;
        this.n2 = n2;
        this.similarity = similarity;
    }
    
    @Override
    public String toString() {
        return n1.id + SEPARATOR + n2.id + SEPARATOR + similarity;
        
    }
    
    public static Edge parseString(String line) {
        String[] values = line.split(SEPARATOR);
        
        return new Edge(
            new Node(values[0]),
            new Node(values[1]),
            Double.valueOf(values[2])
        );
        
    }


    @Override
    public int compareTo(Object other) {
        if (! other.getClass().isInstance(this)) {
            throw new InvalidParameterException();
        }
        
        if (this.similarity < ((Edge) other).similarity) {
            return -1;
        } else if (this.similarity > ((Edge) other).similarity) {
            return 1;
        }
        
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (! obj.getClass().isInstance(this)) {
            return false;
        }
        
        Edge other = (Edge) obj;        
        return n1.id.equals(other.n1.id) &&
                n2.id.equals(other.n2.id);
        
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.n1 != null ? this.n1.hashCode() : 0);
        hash = 89 * hash + (this.n2 != null ? this.n2.hashCode() : 0);
        return hash;
    }
}
