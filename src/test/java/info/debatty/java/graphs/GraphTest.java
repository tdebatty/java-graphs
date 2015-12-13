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
import info.debatty.java.graphs.build.NNDescent;
import info.debatty.java.graphs.build.ThreadedNNDescent;
import java.util.ArrayList;
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
    public void testSearch_5args() {
        // Generate some nodes
        int count = 1000;
        ArrayList<Node> nodes = new ArrayList<Node>(count);
        for (int i = 0; i < count; i++) {
            // The value of our nodes will be an int
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
        GraphBuilder builder = new NNDescent<Integer>();
        builder.setK(10);
        builder.setSimilarity(similarity);

        // Run the algorithm, and get the resulting neighbor lists
        Graph<Integer> graph = builder.computeGraph(nodes);
        
        
        NeighborList results = graph.search(
                1010,
                1,
                similarity,
                900);
        assertEquals(nodes.get(999), results.peek().node);
    }

    
}
