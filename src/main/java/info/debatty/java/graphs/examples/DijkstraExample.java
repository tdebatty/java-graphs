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

import info.debatty.java.graphs.Dijkstra;
import info.debatty.java.graphs.Graph;
import info.debatty.java.graphs.SimilarityInterface;
import info.debatty.java.graphs.build.Brute;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;

/**
 *
 * @author Thibault Debatty
 */
public class DijkstraExample {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException, Exception {

        // Create some nodes
        Random rand = new Random();
        LinkedList<Double> nodes = new LinkedList<Double>();
        for (int i = 0; i < 20; i++) {
            nodes.add(rand.nextDouble() * 100);
        }

        // Build the graph
        Brute<Double> builder = new Brute<Double>();
        builder.setK(10);
        builder.setSimilarity(new SimilarityInterface<Double>() {

            public double similarity(Double node1, Double node2) {
                return 1 / (1 + Math.abs(node1 - node2));
            }
        });
        Graph<Double> graph = builder.computeGraph(nodes);
        System.out.println(graph);

        // Compute shortest paths from node0 to all other nodes
        Dijkstra dijkstra = new Dijkstra(graph, nodes.get(0));

        System.out.println(dijkstra.getPath(nodes.get(1)));
        System.out.println(dijkstra.getLargestDistance());

        graph.writeGEXF("example.gexf");
    }
}
