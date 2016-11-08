package info.debatty.java.graphs;

/**
 * Represent a weighted edge (a link from node n1 to node n2)
 *
 * @author Thibault Debatty
 */
public class Edge {

    public NodeInterface n1;
    public NodeInterface n2;
    public double weight = 0;

    public static final String SEPARATOR = ";";

    public Edge() {

    }

    public Edge(NodeInterface n1, NodeInterface n2, double weight) {
        this.n1 = n1;
        this.n2 = n2;
        this.weight = weight;
    }

    @Override
    public String toString() {
        return n1.getId() + SEPARATOR + n2.getId() + SEPARATOR + weight;

    }
}
