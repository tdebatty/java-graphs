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
import info.debatty.java.graphs.SimilarityInterface;
import java.util.LinkedList;
import java.util.Random;
import static junit.framework.Assert.assertEquals;
import junit.framework.TestCase;

/**
 *
 * @author Thibault Debatty
 */
public class ThreadedNNDescentTest extends TestCase {

    private static final double MIN_CORRECT_RATIO = 0.95;

    public void testComputeGraph() {
        System.out.println("computeGraph");
        System.out.println("============");

        int count = 8123;
        int k = 10;

        // Generate some nodes
        Random r = new Random();
        LinkedList<Integer> nodes = new LinkedList<Integer>();
        for (int i = 0; i < count; i++) {
            nodes.add(r.nextInt());
        }

        // Define the similarity
        SimilarityInterface<Integer> similarity =
                new SimilarityInterface<Integer>() {

            public double similarity(
                    final Integer value1, final Integer value2) {
                return 1.0 / (1.0 + Math.abs(value1 - value2));
            }
        };

        System.out.println("NNdescent threaded graph builder...");
        ThreadedNNDescent<Integer> threaded_builder =
                new ThreadedNNDescent<Integer>();
        threaded_builder.setK(k);
        threaded_builder.setSimilarity(similarity);
        Graph<Integer> threaded_graph = threaded_builder.computeGraph(nodes);

        System.out.println("Brute force threaded graph builder...");
        ThreadedBrute<Integer> builder = new ThreadedBrute<Integer>();
        builder.setK(k);
        builder.setSimilarity(similarity);
        Graph<Integer> graph = builder.computeGraph(nodes);

        Integer first_node = graph.first();
        System.out.println(graph.getNeighbors(first_node));
        System.out.println(threaded_graph.getNeighbors(first_node));
        assertEquals(
                k,
                graph.getNeighbors(first_node)
                        .countCommons(threaded_graph.getNeighbors(first_node)));

        int correct_edges = threaded_graph.compare(graph);
        double correct_ratio = 1.0 * correct_edges / (count * k);

        System.out.println("Correct edges: " + correct_edges + " : "
                + correct_ratio);

        assertEquals(count, threaded_graph.size());
        assertTrue("Too many wrong edges!", correct_ratio >= MIN_CORRECT_RATIO);
    }

    /**
     * Test that the threaded nndescent algorithms produces neighborlists of
     * the correct size (might fail because of missing synchronization).
     */
    public final void testK() {
        System.out.println("test K");
        System.out.println("======");

        int count = 10;
        int k = 5;
        int trials = 10;

        // Generate some nodes
        Random r = new Random();
        LinkedList<Integer> nodes = new LinkedList<Integer>();
        for (int i = 0; i < count; i++) {
            nodes.add(r.nextInt());
        }

        // Define the similarity
        SimilarityInterface<Integer> similarity =
                new SimilarityInterface<Integer>() {

            public double similarity(
                    final Integer value1, final Integer value2) {
                return 1.0 / (1.0 + Math.abs(value1 - value2));
            }
        };

        for (int i = 0; i < trials; i++) {
            ThreadedNNDescent<Integer> threaded_builder =
                    new ThreadedNNDescent<Integer>();
            threaded_builder.setK(k);
            threaded_builder.setSimilarity(similarity);
            Graph<Integer> threaded_graph =
                    threaded_builder.computeGraph(nodes);

            for (Integer value : threaded_graph.getNodes()) {
                assertEquals(k, threaded_graph.getNeighbors(value).size());
            }
        }
    }

}
