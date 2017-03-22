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
package info.debatty.java.graphs.examples;

import info.debatty.java.graphs.*;
import info.debatty.java.graphs.build.NNDescent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class NNDescentExample {

    public static void main(String[] args) {
        Random r = new Random();
        int count = 1000;
        int k = 10;

        // Create the nodes
        ArrayList<Integer> nodes = new ArrayList<Integer>(count);
        for (int i = 0; i < count; i++) {
            // The value of our nodes will be an int
            nodes.add(r.nextInt(10 * count));
        }

        // Instantiate and configure the build algorithm
        NNDescent builder = new NNDescent();
        builder.setK(k);

        // early termination coefficient
        builder.setDelta(0.1);

        // sampling coefficient
        builder.setRho(0.2);

        builder.setMaxIterations(10);

        builder.setSimilarity(new SimilarityInterface<Integer>() {

            @Override
            public double similarity(Integer v1, Integer v2) {
                return 1.0 / (1.0 + Math.abs(v1 - v2));
            }
        });

        // Run the algorithm and get computed graph
        Graph<Integer> graph = builder.computeGraph(nodes);

        // Display neighborlists
        for (Integer n : nodes) {
            NeighborList nl = graph.getNeighbors(n);
            System.out.print(n);
            System.out.println(nl);
        }

        // Optionnally, we can test the builder
        // This will compute the approximate graph, and then the exact graph
        // and compare results...
        builder.test(nodes);

        // Analyze the graph:
        // Count number of connected components
        System.out.println(graph.connectedComponents().size());

        // Search a query (fast approximative algorithm)
        System.out.println(graph.fastSearch(r.nextInt(10 * count), 1));

        // Count number of strongly connected components
        System.out.println(graph.stronglyConnectedComponents().size());

        // Now we can add a node to the graph (using a fast approximate algorithm)
        graph.fastAdd(r.nextInt(10 * count));
    }
}
