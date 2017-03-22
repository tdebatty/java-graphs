package info.debatty.java.graphs.build;

import info.debatty.java.graphs.Graph;
import info.debatty.java.graphs.SimilarityInterface;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Thibault Debatty
 * @param <T> the actual type of the nodes
 */
public abstract class GraphBuilder<T> implements Serializable {

    /**
     * Default number of neighbors.
     */
    public static final int DEFAULT_K = 10;

    // Build parameters
    private int k = DEFAULT_K;
    private SimilarityInterface<T> similarity = null;

    // Builder state
    private SimilarityCounter<T> similarity_counter;


    /**
     *
     * @return
     */
    public final int getK() {
        return k;
    }

    /**
     * Define k the number of edges per node. Default value is 10
     *
     * @param k
     */
    public final void setK(final int k) {
        if (k <= 0) {
            throw new IllegalArgumentException("k must be > 0");
        }
        this.k = k;
    }

    /**
     *
     * @return
     */
    public final SimilarityInterface getSimilarity() {
        return similarity;
    }

    /**
     *
     * @param similarity
     */
    public final void setSimilarity(final SimilarityInterface<T> similarity) {
        this.similarity = similarity;
    }

    /**
     *
     * @return
     */
    public final int getComputedSimilarities() {
        return similarity_counter.getCount();
    }

    /**
     * Compute the graph.
     * @param nodes
     * @return
     */
    public final Graph<T> computeGraph(final List<T> nodes) {

        if (similarity == null) {
            throw new IllegalArgumentException("Similarity is not defined");
        }
        similarity_counter =
                new SimilarityCounter<T>(similarity);
        Graph<T> graph = computeGraph(nodes, k, similarity_counter);
        graph.setK(k);
        graph.setSimilarity(similarity);
        return graph;
    }

    /**
     * Build the approximate graph, then use brute-force to build the exact
     * graph and compare the results.
     *
     * @param nodes
     */
    public final void test(final List<T> nodes) {
        Graph<T> approximate_graph = computeGraph(nodes);

        // Use Brute force to build the exact graph
        Brute brute = new Brute();
        brute.setK(k);
        brute.setSimilarity(similarity);
        Graph<T> exact_graph = brute.computeGraph(nodes);

        int correct = 0;
        for (T node : nodes) {
            correct += approximate_graph.getNeighbors(node)
                    .countCommons(exact_graph.getNeighbors(node));
        }

        System.out.println(
                "Computed similarities: " + this.getComputedSimilarities());
        double speedup_ratio
                = (double) (nodes.size() * (nodes.size() - 1) / 2)
                / this.getComputedSimilarities();
        System.out.println("Speedup ratio: " + speedup_ratio);

        double correct_ratio = (double) correct / (nodes.size() * k);
        System.out.println("Correct edges: " + correct
                + " (" + correct_ratio * 100 + "%)");

        System.out.println("Quality-equivalent speedup: "
                + speedup_ratio * correct_ratio);
    }

    /**
     *
     * @param path
     * @return
     * @throws java.io.FileNotFoundException if file does not exist
     * @throws IOException if file cannot be read
     */
    public static LinkedList<String> readFile(final String path)
            throws FileNotFoundException, IOException {

            BufferedReader reader = new BufferedReader(
                    new FileReader(path));
            LinkedList<String> nodes = new LinkedList<String>();
            String line;
            while ((line = reader.readLine()) != null) {
                nodes.add(line);
            }
            reader.close();
            return nodes;
    }

    protected abstract Graph<T> computeGraph(
            List<T> nodes, int k, SimilarityInterface<T> similarity);
}
