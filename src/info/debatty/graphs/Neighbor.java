package info.debatty.graphs;

/**
 *
 * @author tibo
 */
public class Neighbor implements Comparable<Neighbor>, Cloneable {
    public Node node;
    public double similarity;
    
    public boolean is_new = true; // only used by sequential nndescent...

    public static final String DELIMITER = ",,,";
    
    public Neighbor(Node node, double similarity) {
        this.node = node;
        this.similarity = similarity;
    }

    public Neighbor() {
        node = new Node();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return new Neighbor(
                (Node) node.clone(),
                similarity);
        
        
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
    public int compareTo(Neighbor other) {
        //if (this.node.equals(other.node)) {
        //    return 0;
        //}
        
        if (this.similarity == other.similarity) {
            return 0;
        }
        
        return this.similarity > other.similarity ? 1 : -1;
    }    
}
