package info.debatty.java.graphs.build;

import info.debatty.java.graphs.CallbackInterface;
import info.debatty.java.graphs.Graph;
import info.debatty.java.graphs.SimilarityInterface;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Thibault Debatty
 * @param <T> the actual type of the nodes
 */
public abstract class GraphBuilder<T> implements Cloneable, Serializable {

    protected int k = 10;
    protected SimilarityInterface<T> similarity;
    protected CallbackInterface callback = null;
    protected int computed_similarities = 0;

    public int getK() {
        return k;
    }

    /**
     * Define k the number of edges per node. Default value is 10
     *
     * @param k
     */
    public void setK(int k) {
        if (k <= 0) {
            throw new InvalidParameterException("k must be > 0");
        }
        this.k = k;
    }

    public SimilarityInterface getSimilarity() {
        return similarity;
    }

    public void setSimilarity(SimilarityInterface<T> similarity) {
        this.similarity = similarity;
    }

    public CallbackInterface getCallback() {
        return callback;
    }

    public void setCallback(CallbackInterface callback) {
        this.callback = callback;
    }

    public int getComputedSimilarities() {
        return computed_similarities;
    }

    public Graph<T> computeGraph(List<T> nodes) {

        if (similarity == null) {
            throw new InvalidParameterException("Similarity is not defined");
        }
        computed_similarities = 0;
        Graph<T> graph = _computeGraph(nodes);
        graph.setK(k);
        graph.setSimilarity(similarity);
        return graph;
    }

    /**
     * Build the approximate graph, then use brute-force to build the exact
     * graph and compare the results
     *
     * @param nodes
     */
    public void test(List<T> nodes) {
        Graph<T> approximate_graph = computeGraph(nodes);

        // Tse Brute force to build the exact graph
        Brute brute = new Brute();
        brute.setK(k);
        brute.setSimilarity(similarity);
        Graph<T> exact_graph = brute.computeGraph(nodes);

        int correct = 0;
        for (T node : nodes) {
            correct += approximate_graph.getNeighbors(node).countCommons(exact_graph.getNeighbors(node));
        }

        System.out.println("Theoretical speedup: " + this.estimatedSpeedup());
        System.out.println("Computed similarities: " + this.getComputedSimilarities());
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

    public double estimatedSpeedup() {
        return 1.0;
    }

    public static LinkedList<String> readFile(String path) {
        try {
            FileReader fileReader;
            fileReader = new FileReader(path);

            BufferedReader bufferedReader = new BufferedReader(fileReader);
            LinkedList<String> nodes = new LinkedList<String>();
            String line;
            int i = 0;
            while ((line = bufferedReader.readLine()) != null) {
                nodes.add(line);
                i++;
            }
            bufferedReader.close();
            return nodes;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GraphBuilder.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GraphBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    protected abstract Graph<T> _computeGraph(List<T> nodes);
}
