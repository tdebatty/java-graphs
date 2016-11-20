package info.debatty.java.graphs;

/**
 * Represent a weighted edge (a link from node n1 to node n2)
 *
 * @author Thibault Debatty
 */
public class Edge<T> {

    public T n1;
    public T n2;
    public double weight = 0;

    public static final String SEPARATOR = ";";

    public Edge() {

    }

    public Edge(T n1, T n2, double weight) {
        this.n1 = n1;
        this.n2 = n2;
        this.weight = weight;
    }

    @Override
    public String toString() {
        return n1.toString() + SEPARATOR + n2.toString()+ SEPARATOR + weight;

    }
}
