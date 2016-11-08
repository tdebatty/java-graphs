/*
 * The MIT License
 *
 * Copyright 2015 Thibault Debatty.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package info.debatty.java.graphs;

import info.debatty.java.graphs.build.Brute;
import info.debatty.java.graphs.build.GraphBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import junit.framework.TestCase;

/**
 *
 * @author Thibault Debatty
 */
public class GraphTest extends TestCase {

    /**
     * Test of connectedComponents method, of class Graph.
     */
    public void testConnectedComponents() {
        // Generate two series of nodes
        ArrayList<Node> nodes = new ArrayList<Node>();
        for (int i = 0; i < 1000; i++) {
            // The value of our nodes will be an int
            nodes.add(new Node<Integer>(String.valueOf(i), i));
            nodes.add(new Node<Integer>(String.valueOf(1000000 + i), 1000000 + i));

        }


        // Instantiate and configure the brute-force graph building algorithm
        // The minimum is to define k (number of edges per node)
        // and a similarity metric between nodes
        GraphBuilder builder = new Brute<Node<Integer>>();
        builder.setK(20);
        builder.setSimilarity(new IntegerNodeSimilarity());

        // Run the algorithm, and get the resulting neighbor lists
        Graph<Node<Integer>> graph = builder.computeGraph(nodes);

        assertEquals(2, graph.connectedComponents().size());
    }

        /**
     * Test of stronglyConnectedComponents method, of class Graph.
     */
    public void testStronglyConnectedComponents() {
        // Generate two series of nodes
        ArrayList<Node<Integer>> nodes = new ArrayList<Node<Integer>>();
        for (int i = 0; i <= 1000; i++) {
            // The value of our nodes will be an int
            nodes.add(new Node<Integer>(String.valueOf(i), i));
        }

        for (int i = 1010; i < 2000; i += 20) {
            // This will generate a link from node 1010 to node 1000
            // but not in the other direction
            // => will not be strongly connected
            nodes.add(new Node<Integer>(String.valueOf(i), i));
        }

        // Instantiate and configure the brute-force graph building algorithm
        // The minimum is to define k (number of edges per node)
        // and a similarity metric between nodes
        Brute<Node<Integer>> builder = new Brute<Node<Integer>>();
        builder.setK(5);
        builder.setSimilarity(new IntegerNodeSimilarity());

        // Run the algorithm, and get the resulting neighbor lists
        Graph<Node<Integer>> graph = builder.computeGraph(nodes);
        assertEquals(2, graph.stronglyConnectedComponents().size());
    }


    /**
     * Test of stronglyConnectedComponents method, of class Graph.
     */
    public void testStronglyConnectedComponents2() {
        System.out.println("Strongly connected components");
        System.out.println("=============================");

        // Generate two series of nodes
        ArrayList<Node<Integer>> nodes = new ArrayList<Node<Integer>>();

        nodes.add(new Node<Integer>("1", 1));
        nodes.add(new Node<Integer>("2", 2));
        nodes.add(new Node<Integer>("3", 3));
        nodes.add(new Node<Integer>("7", 7));
        nodes.add(new Node<Integer>("8", 8));
        nodes.add(new Node<Integer>("9", 9));


        // Instantiate and configure the brute-force graph building algorithm
        // The minimum is to define k (number of edges per node)
        // and a similarity metric between nodes
        Brute<Node<Integer>> builder = new Brute<Node<Integer>>();
        builder.setK(2);
        builder.setSimilarity(new IntegerNodeSimilarity());

        // Run the algorithm, and get the resulting neighbor lists
        Graph<Node<Integer>> graph = builder.computeGraph(nodes);
        System.out.println(graph.stronglyConnectedComponents());
        assertEquals(2, graph.stronglyConnectedComponents().size());
    }

    /**
     * Test of search method, of class Graph.
     *
     * @throws java.lang.InterruptedException
     * @throws java.util.concurrent.ExecutionException
     */
    public void testSearch() throws InterruptedException, ExecutionException {
        System.out.println("Search");
        System.out.println("======");

        int n = 4000;

        SimilarityInterface<Double> similarity = new SimilarityInterface<Double>() {

            public double similarity(Double value1, Double value2) {
                return 1.0 / (1 + Math.abs(value1 - value2));
            }
        };

        System.out.println("create some random nodes...");
        Random rand = new Random();
        List<Node<Double>> data = new ArrayList<Node<Double>>();
        while (data.size() < n) {
            data.add(new Node<Double>(String.valueOf(data.size()), 100.0 + 100.0 * rand.nextGaussian()));
            data.add(new Node<Double>(String.valueOf(data.size()), 150.0 + 100.0 * rand.nextGaussian()));
            data.add(new Node<Double>(String.valueOf(data.size()), 300.0 + 100.0 * rand.nextGaussian()));
        }

        System.out.println("compute graph...");
        GraphBuilder builder = new Brute<Node<Double>>();
        builder.setK(10);
        builder.setSimilarity(similarity);
        Graph<Node<Double>> graph = builder.computeGraph(data);

        System.out.println("perform evaluation...");
        int correct = 0;
        for (int i = 0; i < 100; i++) {
            double query = 400.0 * rand.nextDouble();

            NeighborList approximate_result = graph.fastSearch(
                    query,
                    1,
                    30);

            // Search the (real) most similar
            NeighborList exhaustive_result = graph.searchExhaustive(query, 1);
            correct += approximate_result.countCommons(exhaustive_result);

        }

        //System.out.println(data.size());
        System.out.println("Found " + correct + " correct results!");
        assertTrue(correct > 50);
    }

    public final void testAdd() {
        System.out.println("Test graph.add()");
        System.out.println("================");

        int n = 1000;

        SimilarityInterface<Double> similarity = new SimilarityInterface<Double>() {

            public double similarity(Double value1, Double value2) {
                return 1.0 / (1 + Math.abs(value1 - value2));
            }
        };

        System.out.println("create some random nodes...");
        Random rand = new Random();
        List<Node<Double>> data = new ArrayList<Node<Double>>();
        while (data.size() < n) {
            data.add(new Node<Double>(String.valueOf(data.size()), 100.0 + 100.0 * rand.nextGaussian()));
            data.add(new Node<Double>(String.valueOf(data.size()), 150.0 + 100.0 * rand.nextGaussian()));
            data.add(new Node<Double>(String.valueOf(data.size()), 300.0 + 100.0 * rand.nextGaussian()));
        }

        System.out.println("Build exact online graph...");
        Graph<Double> online_graph = new Graph<Double>();
        online_graph.setSimilarity(similarity);
        for (Node<Double> node : data) {
            online_graph.add(node);
        }

        System.out.println("Build reference graph...");
        GraphBuilder builder = new Brute<Double>();
        builder.setK(10);
        builder.setSimilarity(similarity);
        Graph<Double> graph = builder.computeGraph(data);

        int correct = 0;
        for (Node<Double> node : data) {
            correct += graph.get(node).countCommons(online_graph.get(node));
        }
        System.out.println("Found " + correct + " correct edges");
        assertEquals(data.size() * 10, correct);

    }

    public void testFastAdd() {
        System.out.println("Fast add");
        System.out.println("========");

        int n = 1000;
        int n_test = 1000;

        SimilarityInterface<Double> similarity = new SimilarityInterface<Double>() {

            public double similarity(Double value1, Double value2) {
                return 1.0 / (1 + Math.abs(value1 - value2));
            }
        };

        System.out.println("create some random nodes...");
        Random rand = new Random();
        List<Node<Double>> data = new ArrayList<Node<Double>>();
        while (data.size() < n) {
            data.add(new Node<Double>(String.valueOf(data.size()), 100.0 + 100.0 * rand.nextGaussian()));
            data.add(new Node<Double>(String.valueOf(data.size()), 150.0 + 100.0 * rand.nextGaussian()));
            data.add(new Node<Double>(String.valueOf(data.size()), 300.0 + 100.0 * rand.nextGaussian()));
            data.add(new Node<Double>(String.valueOf(data.size()), 200.0 + 50.0 * rand.nextGaussian()));
        }

        System.out.println("Compute initial graph using exhaustive search...");
        Graph<Double> graph = new Graph<Double>();
        graph.setK(10);
        graph.setSimilarity(similarity);
        for (Node<Double> node : data) {
            graph.add(node);
        }

        System.out.println("Add some nodes using fast algorithm...");
        for (int i = 0; i < n_test; i++) {
            Node<Double> query = new Node<Double>(
                    String.valueOf(n + i), 400.0 * rand.nextDouble());

            StatisticsContainer stats = new StatisticsContainer();
            graph.fastAdd(
                    query,
                    Graph.DEFAULT_SEARCH_SPEEDUP,
                    Graph.DEFAULT_SEARCH_RANDOM_JUMPS,
                    Graph.DEFAULT_SEARCH_EXPANSION,
                    stats);
            System.out.println(stats);
            data.add(query);
        }

        assertEquals(n + n_test, graph.size());

        System.out.println("Compute validation graph...");
        GraphBuilder builder = new Brute<Double>();
        builder.setK(10);
        builder.setSimilarity(similarity);
        Graph validation_graph = builder.computeGraph(data);

        int correct = 0;
        for (Node<Double> node : data) {
            correct += graph.get(node).countCommons(validation_graph.get(node));
        }
        System.out.println("Found " + correct + " correct edges");
        System.out.printf("= %f \n", 100.0 * correct / (10 * data.size()));
        assertTrue(1.0 * correct / (10 * data.size()) > 0.5);

    }

    public void testRemove() {
        System.out.println("Fast remove");
        System.out.println("===========");

        int n = 1000;
        int n_test = 100;

        SimilarityInterface<Double> similarity = new SimilarityInterface<Double>() {

            public double similarity(Double value1, Double value2) {
                return 1.0 / (1 + Math.abs(value1 - value2));
            }
        };

        System.out.println("create some random nodes...");
        Random rand = new Random();
        List<Node<Double>> data = new ArrayList<Node<Double>>();
        while (data.size() < n) {
            data.add(new Node<Double>(String.valueOf(data.size()), 100.0 + 100.0 * rand.nextGaussian()));
            data.add(new Node<Double>(String.valueOf(data.size()), 150.0 + 100.0 * rand.nextGaussian()));
            data.add(new Node<Double>(String.valueOf(data.size()), 300.0 + 100.0 * rand.nextGaussian()));
            data.add(new Node<Double>(String.valueOf(data.size()), 200.0 + 50.0 * rand.nextGaussian()));
        }

        System.out.println("Compute initial graph using exhaustive search...");
        Graph<Double> graph = new Graph<Double>();
        graph.setK(10);
        graph.setSimilarity(similarity);
        for (Node<Double> node : data) {
            graph.add(node);
        }

        System.out.println("Remove some nodes...");
        for (int i = 0; i < n_test; i++) {
            Node<Double> query = data.get(rand.nextInt(data.size() - 1));

            graph.fastRemove(query);
            data.remove(query);

            for (Node node : graph.getNodes()) {
                assertEquals(10, graph.get(node).size());
                assertTrue(
                        "Graph still contains references to deleted node!!!",
                        !graph.get(node).containsNode(query));
            }
        }

        assertEquals(n - n_test, graph.size());
    }

    public void testWindow() {
        System.out.println("Window");
        System.out.println("======");

        int n = 1000;
        int n_test = 1000;

        SimilarityInterface<Double> similarity = new SimilarityInterface<Double>() {

            public double similarity(Double value1, Double value2) {
                return 1.0 / (1 + Math.abs(value1 - value2));
            }
        };

        System.out.println("create some random nodes...");
        Random rand = new Random();
        List<Node<Double>> data = new ArrayList<Node<Double>>();
        while (data.size() < n) {
            data.add(new Node<Double>(String.valueOf(data.size()), 100.0 + 100.0 * rand.nextGaussian()));
            data.add(new Node<Double>(String.valueOf(data.size()), 150.0 + 100.0 * rand.nextGaussian()));
            data.add(new Node<Double>(String.valueOf(data.size()), 300.0 + 100.0 * rand.nextGaussian()));
            data.add(new Node<Double>(String.valueOf(data.size()), 200.0 + 50.0 * rand.nextGaussian()));
        }

        System.out.println("Compute initial graph using exhaustive search...");
        Graph<Double> graph = new Graph<Double>();
        graph.setK(10);
        graph.setSimilarity(similarity);
        for (Node<Double> node : data) {
            graph.add(node);
        }

        graph.setWindowSize(n);

        System.out.println("add some nodes...");
        for (int i = 0; i < n_test; i++) {
            Node<Double> query = new Node<Double>(
                    String.valueOf(n + i), 400.0 * rand.nextDouble());

            graph.fastAdd(query);
            data.add(query);

            for (Node<Double> node : graph.getNodes()) {
                if (graph.get(node) == null) {
                    System.err.println("NL is null!!");
                }
            }
        }

        assertEquals(n, graph.size());
    }


    public void testAddNodeSameId() {
        System.out.println("addNode with same id");

        int n = 1000;

        SimilarityInterface<Double> similarity = new SimilarityInterface<Double>() {

            public double similarity(Double value1, Double value2) {
                return 1.0 / (1 + Math.abs(value1 - value2));
            }
        };

        System.out.println("create some random nodes...");
        Random rand = new Random();
        List<Node<Double>> data = new ArrayList<Node<Double>>();
        while (data.size() < n) {
            data.add(new Node<Double>(String.valueOf(data.size()), 100.0 + 100.0 * rand.nextGaussian()));
            data.add(new Node<Double>(String.valueOf(data.size()), 150.0 + 100.0 * rand.nextGaussian()));
            data.add(new Node<Double>(String.valueOf(data.size()), 300.0 + 100.0 * rand.nextGaussian()));
            data.add(new Node<Double>(String.valueOf(data.size()), 200.0 + 50.0 * rand.nextGaussian()));
        }

        System.out.println("Compute initial graph using exhaustive search...");
        Graph<Double> graph = new Graph<Double>();
        graph.setK(10);
        graph.setSimilarity(similarity);
        for (Node<Double> node : data) {
            graph.add(node);
        }

        System.out.println("Add a node with same id...");
        try {
            graph.fastAdd(data.get(1));
            fail("Should throw exception!!");
        } catch (IllegalArgumentException ex) {
        }
    }
}

class IntegerNodeSimilarity implements SimilarityInterface<Node<Integer>> {

    public double similarity(Node<Integer> value1, Node<Integer> value2) {
        return 1.0 / (1.0 + Math.abs(value1.value - value2.value));
    }
};