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
import info.debatty.java.graphs.NeighborList;
import info.debatty.java.graphs.SimilarityInterface;
import info.debatty.java.graphs.build.Brute;
import info.debatty.java.graphs.build.GraphBuilder;
import info.debatty.java.stringsimilarity.JaroWinkler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class SearchExample {

    public static void main(String[] args)
            throws InterruptedException, ExecutionException, IOException {
        int tests = 100;

        // Number of neighbors to search
        int k = 1;
        // Number of similarities to compute using approximate search
        int max_similaritites = 100;

        // Read the file
        List<String> nodes = GraphBuilder.readFile(
                SearchExample.class.getClassLoader()
                        .getResource("726-unique-spams").getFile());

        // Leave some random nodes out for the search queries
        Random rand = new Random();
        ArrayList<String> queries = new ArrayList<String>(tests);
        for (int i = 0; i < tests; i++) {
            queries.add(nodes.remove(rand.nextInt(nodes.size())));
        }

        // Define the similarity to use
        SimilarityInterface<String> similarity
                = new SimilarityInterface<String>() {

                    public double similarity(String value1, String value2) {
                        JaroWinkler jw = new JaroWinkler();
                        return jw.similarity(value1, value2);
                    }
                };

        // Compute the graph
        Brute<String> builder = new Brute();
        builder.setSimilarity(similarity);
        builder.setK(20);
        Graph<String> graph = builder.computeGraph(nodes);

        // Perform some research...
        int correct = 0;

        for (String query : queries) {

            // Perform GNNS
            System.out.println("Query: " + query);
            NeighborList resultset_gnss = graph.fastSearch(query);
            System.out.println(resultset_gnss);

            // Perform linear search
            NeighborList resultset_linear = graph.search(query, k);
            System.out.println(resultset_linear);

            correct += resultset_gnss.countCommons(resultset_linear);
        }

        System.out.println("Correct: " + correct + " / " + tests);
        System.out.println("Computed similarities (approximate search): "
                + queries.size() * max_similaritites);
        System.out.println("Computed similarities (exhaustive search): "
                + nodes.size() * tests);

        System.out.println("Quality equivalent speedup: "
                + (double) nodes.size() * tests * correct
                / tests
                / (queries.size() * max_similaritites));
    }
}
