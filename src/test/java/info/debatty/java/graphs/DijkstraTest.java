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
import java.util.ArrayList;
import junit.framework.TestCase;

/**
 *
 * @author Thibault Debatty
 */
public class DijkstraTest extends TestCase {

    /**
     * Test of getPath method, of class Dijkstra.
     *
     * @throws java.lang.Exception if no path is found between the two nodes
     */
    public final void testGetPath() throws Exception  {

        // Generate some nodes
        int count = 1000;
        ArrayList<Node> nodes = new ArrayList<Node>(count);
        for (int i = 0; i < count; i++) {
            // The value of our nodes will be an int
            nodes.add(new Node<Integer>(String.valueOf(i), i));
        }

        // Instantiate and configure the brute-force graph building algorithm
        // The minimum is to define k (number of edges per node)
        // and a similarity metric between nodes
        Brute builder = new Brute<Integer>();
        builder.setK(10);
        builder.setSimilarity(new SimilarityInterface<Integer>() {

            public double similarity(
                    final Integer value1,
                    final Integer value2) {
                return 1.0 / (1.0 + Math.abs(value1 - value2));
            }
        });

        // Run the algorithm, and get the resulting neighbor lists
        Graph<Integer> graph = builder.computeGraph(nodes);

        // Compute shortest paths from node0 to all other nodes
        Dijkstra dijkstra = new Dijkstra(graph, nodes.get(0));
        assertEquals(200, dijkstra.getPath(nodes.get(999)).size());

    }

    /**
     * Test of getLargestDistance method, of class Dijkstra.
     */
    public final void testGetLargestDistance() {
        // Generate some nodes
        int count = 1000;
        ArrayList<Node> nodes = new ArrayList<Node>(count);
        for (int i = 0; i < count; i++) {
            // The value of our nodes will be an int
            nodes.add(new Node<Integer>(String.valueOf(i), i));
        }

        // Instantiate and configure the brute-force graph building algorithm
        // The minimum is to define k (number of edges per node)
        // and a similarity metric between nodes
        Brute builder = new Brute<Integer>();
        builder.setK(10);
        builder.setSimilarity(new SimilarityInterface<Integer>() {

            public double similarity(
                    final Integer value1,
                    final Integer value2) {
                return 1.0 / (1.0 + Math.abs(value1 - value2));
            }
        });

        // Run the algorithm, and get the resulting neighbor lists
        Graph<Integer> graph = builder.computeGraph(nodes);

        // Compute shortest paths from node0 to all other nodes
        Dijkstra dijkstra = new Dijkstra(graph, nodes.get(0));

        assertEquals(199, dijkstra.getLargestDistance());
    }
}
