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
import info.debatty.java.graphs.build.ThreadedNNDescent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import junit.framework.TestCase;

/**
 *
 * @author Thibault Debatty
 */
public class GraphTest extends TestCase {
    
    public GraphTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }


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
        
        SimilarityInterface<Integer> similarity = new SimilarityInterface<Integer>() {

            public double similarity(Integer value1, Integer value2) {
                return 1.0 / (1.0 + Math.abs(value1 - value2));
            }
        };

        // Instantiate and configure the brute-force graph building algorithm
        // The minimum is to define k (number of edges per node)
        // and a similarity metric between nodes
        GraphBuilder builder = new ThreadedNNDescent<Integer>();
        builder.setK(20);
        builder.setSimilarity(similarity);

        // Run the algorithm, and get the resulting neighbor lists
        Graph<Integer> graph = builder.computeGraph(nodes);
        
        assertEquals(2, graph.connectedComponents().size());
    }

    /**
     * Test of stronglyConnectedComponents method, of class Graph.
     */
    public void testStronglyConnectedComponents() {
        // Generate two series of nodes
        ArrayList<Node> nodes = new ArrayList<Node>();
        for (int i = 0; i <= 1000; i++) {
            // The value of our nodes will be an int
            nodes.add(new Node<Integer>(String.valueOf(i), i));
        }
        for (int i = 1010; i < 2000; i+=20) {
            // This will generate a link from node 1010 to node 1000
            // but not in the other direction
            // => will not be strongly connected
            nodes.add(new Node<Integer>(String.valueOf(i), i));
        }
        
        
        SimilarityInterface<Integer> similarity = new SimilarityInterface<Integer>() {

            public double similarity(Integer value1, Integer value2) {
                return 1.0 / (1.0 + Math.abs(value1 - value2));
            }
        };

        // Instantiate and configure the brute-force graph building algorithm
        // The minimum is to define k (number of edges per node)
        // and a similarity metric between nodes
        GraphBuilder builder = new Brute<Integer>();
        builder.setK(5);
        builder.setSimilarity(similarity);

        // Run the algorithm, and get the resulting neighbor lists
        Graph<Integer> graph = builder.computeGraph(nodes);
        assertEquals(2, graph.stronglyConnectedComponents().size());
    }

    /**
     * Test of search method, of class Graph.
     */
    public void testSearch() {
        System.out.println("search");
        
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
        GraphBuilder builder = new Brute<Double>();
        builder.setK(10);
        builder.setSimilarity(similarity);
        Graph<Double> graph = builder.computeGraph(data);

        System.out.println("perform evaluation...");
        int correct = 0;
        for (int i = 0; i < 100; i++) {
            double query = 400.0 * rand.nextDouble();
            
            NeighborList results = graph.search(
                query,
                1);
            
            // Search the (real) most similar
            double highest_similarity = -1;
            Node most_similar = null;
            for (Node<Double> node : data) {
                double sim = similarity.similarity(query, node.value);
                if (sim > highest_similarity) {
                    highest_similarity = sim;
                    most_similar = node;
                }
            }
            
            // Check if result is correct
            if (results.peek().node.equals(most_similar)) {
                correct++;
            }
        }

        //System.out.println(data.size());
        System.out.println("Found " + correct + " correct results!");
        assertEquals(true, correct > 50);
    }
}
