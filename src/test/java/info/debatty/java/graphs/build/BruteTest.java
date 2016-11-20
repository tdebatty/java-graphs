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
package info.debatty.java.graphs.build;

import info.debatty.java.graphs.Graph;
import info.debatty.java.graphs.SimilarityInterface;
import java.util.LinkedList;
import junit.framework.TestCase;

/**
 *
 * @author Thibault Debatty
 */
public class BruteTest extends TestCase {

    public BruteTest(String testName) {
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

    public void testComputeGraph() {
        System.out.println("computeGraph");

        // Generate some nodes
        int count = 1000;
        LinkedList<Integer> nodes = new LinkedList<Integer>();
        for (int i = 0; i < count; i++) {
            // The value of our nodes will be an int
            nodes.add(i);
        }

        // Instantiate and configure the brute-force graph building algorithm
        // The minimum is to define k (number of edges per node)
        // and a similarity metric between nodes
        Brute<Integer> builder = new Brute<Integer>();
        builder.setK(10);
        builder.setSimilarity(new SimilarityInterface<Integer>() {

            public double similarity(Integer node1, Integer node2) {
                return 1.0 / (1.0 + Math.abs(node1 - node2));
            }
        });

        // Run the algorithm, and get the resulting neighbor lists
        Graph<Integer> graph = builder.computeGraph(nodes);

        // Test...
        Integer node_0 = nodes.get(0);

        // this is the k'st most similar node (=> number 9)
        Integer other_node = (Integer) graph.getNeighbors(node_0).peek().node;

        assertEquals(other_node.intValue(), 10);

    }
}
