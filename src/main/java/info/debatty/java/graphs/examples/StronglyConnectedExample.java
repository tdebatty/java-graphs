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
import info.debatty.java.graphs.build.Brute;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author Thibault Debatty
 */
public class StronglyConnectedExample {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Create some nodes
        Random rand = new Random();
        ArrayList<Double> nodes = new ArrayList<Double>();
        for (int i = 0; i < 100; i++) {
            nodes.add(rand.nextDouble() * 100);
            nodes.add(1000000 + rand.nextDouble() * 100);
        }

        // Build the graph
        Brute<Double> builder = new Brute<Double>();
        builder.setK(10);
        builder.setSimilarity(new SimilarityInterface<Double>() {

            public double similarity(Double value1, Double value2) {
                return 1 / (1 + Math.abs(value1 - value2));
            }
        });
        Graph<Double> graph = builder.computeGraph(nodes);

        ArrayList<Graph<Double>> stronglyConnectedComponents = graph.stronglyConnectedComponents();

        System.out.printf("Found %d strongly connected components\n", stronglyConnectedComponents.size());

        for (Graph<Double> component : stronglyConnectedComponents) {
            System.out.printf("Contains %d nodes\n", component.size());
            System.out.println(component);
        }
    }

}
