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

import info.debatty.java.graphs.Graph;
import info.debatty.java.graphs.SimilarityInterface;
import info.debatty.java.graphs.build.ThreadedNNDescent;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class PruneExample {

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        Random r = new Random();
        int count = 100000;
        int k = 20;

        // Create the nodes
        ArrayList<Integer> nodes = new ArrayList<Integer>(count);
        for (int i = 0; i < count; i++) {
            // The value of our nodes will be an int
            nodes.add(r.nextInt());
        }

        // Instantiate and configure the build algorithm
        ThreadedNNDescent builder = new ThreadedNNDescent();
        builder.setK(k);
        builder.setDelta(0.1);
        builder.setRho(0.5);

        builder.setSimilarity(new SimilarityInterface<Integer>() {

            @Override
            public double similarity(final Integer v1, final Integer v2) {
                return 1.0 / (1.0 + Math.abs(v1 - v2));
            }
        });

        // Run the algorithm and get computed graph
        Graph<Integer> graph = builder.computeGraph(nodes);

        System.out.println("Start prunning...");
        long start = System.nanoTime();
        graph.prune(0.5);
        long end = System.nanoTime();
        System.out.println("n = " + count);
        System.out.println("time: " + (end - start));
    }
}
