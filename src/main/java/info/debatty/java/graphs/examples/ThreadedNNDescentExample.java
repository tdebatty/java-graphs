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
import info.debatty.java.graphs.build.ThreadedNNDescent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 *
 * @author Thibault Debatty
 */
public class ThreadedNNDescentExample {

    public static void main(String[] args) {
        Random r = new Random();
        int count = 1000;

        int k = 10;

        ArrayList<Double> nodes = new ArrayList<Double>(count);
        for (int i = 0; i < count; i++) {
            // The value of our nodes will be an int
            nodes.add(r.nextDouble());
        }

        SimilarityInterface<Double> similarity = new SimilarityInterface<Double>() {

            public double similarity(Double value1, Double value2) {
                return 1.0 / (1.0 + Math.abs(value1 - value2));
            }
        };

        // Instantiate and configure the algorithm
        ThreadedNNDescent<Double> builder = new ThreadedNNDescent<Double>();
        builder.setK(k);
        builder.setSimilarity(similarity);
        builder.setMaxIterations(20);
        builder.setDelta(0.1);
        builder.setRho(0.5);

        // Run the algorithm and get computed neighbor lists
        Graph<Double> graph = builder.computeGraph(nodes);

        // Display neighbor lists
        for (Double n : nodes) {
            NeighborList nl = graph.getNeighbors(n);
            System.out.print(n);
            System.out.println(nl);
        }

        // Optionnally, we can test the builder
        // This will compute the approximate graph, and then the exact graph
        // and compare results...
        builder.test(nodes);
    }
}
