/*
 * The MIT License
 *
 * Copyright 2016 Thibault Debatty.
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
import info.debatty.java.graphs.Node;
import info.debatty.java.graphs.SimilarityInterface;
import java.util.ArrayList;
import java.util.Random;
import static junit.framework.Assert.assertEquals;
import junit.framework.TestCase;

/**
 *
 * @author Thibault Debatty
 */
public class ThreadedBruteTest extends TestCase {

    public ThreadedBruteTest(String testName) {
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

        int count = 5500;
        int k = 10;

        // Generate some nodes
        Random r = new Random();
        ArrayList<Node<Integer>> nodes = new ArrayList<Node<Integer>>(count);
        for (int i = 0; i < count; i++) {
            // The value of our nodes will be an int
            nodes.add(new Node<Integer>(String.valueOf(i), r.nextInt(100 * count)));
        }
        // Define the similarity
        SimilarityInterface<Integer> similarity =
                new SimilarityInterface<Integer>() {

            public double similarity(Integer value1, Integer value2) {
                return 1.0 / (1.0 + Math.abs(value1 - value2));
            }
        };

        System.out.println("Multi threaded graph builder...");
        ThreadedBrute<Integer> threaded_builder = new ThreadedBrute<Integer>();
        threaded_builder.setK(k);
        threaded_builder.setSimilarity(similarity);
        Graph<Integer> threaded_graph = threaded_builder.computeGraph(nodes);


        System.out.println("Single thread graph builder...");
        Brute builder = new Brute<Integer>();
        builder.setK(k);
        builder.setSimilarity(similarity);
        Graph<Integer> graph = builder.computeGraph(nodes);

        Node<Integer> first_node = graph.first();
        System.out.println(threaded_graph.get(first_node));
        assertEquals(graph.get(first_node).countCommons(threaded_graph.get(first_node)), k);

        int correct_edges = 0;
        for (Node n : nodes) {
            correct_edges += graph.get(n).countCommons(threaded_graph.get(n));
        }

        assertEquals(count, graph.size());
        assertEquals(count * k, correct_edges);
    }

}
