package info.debatty.java.graphs;


/**
 *
 * @author tibo
 */
public class Edge {
    public Node n1;
    public Node n2;
    public double weight = 0;
    
    public static final String SEPARATOR = ";";
    
    public Edge() {
        
    }

    public Edge(Node n1, Node n2, double weight) {
        this.n1 = n1;
        this.n2 = n2;
        this.weight = weight;
    }
    
    @Override
    public String toString() {
        return n1.id + SEPARATOR + n2.id + SEPARATOR + weight;
        
    }
}
