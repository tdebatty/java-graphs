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

import info.debatty.java.graphs.CallbackInterface;
import info.debatty.java.graphs.NeighborList;
import info.debatty.java.graphs.Node;
import info.debatty.java.graphs.SimilarityInterface;
import info.debatty.java.graphs.build.Brute;
import info.debatty.java.graphs.build.ThreadedNNDescent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 *
 * @author tibo
 */
public class ThreadedNNDescentExample {

    public static void main(String[] args) {
        Random r = new Random();
        int count = 1000;
        
        int k = 10;
        
        ArrayList<Node<Double>> nodes = new ArrayList<Node<Double>>(count);
        for (int i = 0; i < count; i++) {
            // The value of our nodes will be an int
            nodes.add(new Node<Double>(String.valueOf(i), r.nextDouble()));
        }
        
        SimilarityInterface<Double> similarity = new SimilarityInterface<Double>() {

            public double similarity(Double value1, Double value2) {
                return 1.0 / (1.0 + Math.abs(value1 - value2));
            }
        };
        
        // Instantiate and configure the algorithm
        ThreadedNNDescent builder = new ThreadedNNDescent<Double>();
        builder.setThreadCount(3);
        builder.setK(k);
        builder.setSimilarity(similarity);
        
        // Optionnally, define callback
        builder.setCallback(new CallbackInterface() {
            @Override
            public void call(HashMap<String, Object> data) {
                System.out.println(data);
            }
        });
        
        // Run the algorithm and get computed neighbor lists
        HashMap<Node, NeighborList> graph = builder.computeGraph(nodes);
        
        // Display neighbor lists
        for (Node n : nodes) {
            NeighborList nl = graph.get(n);
            System.out.print(n);
            System.out.println(nl);
        }
        
        // Compare with brute-force algorithm
        Brute brute = new Brute<Double>();
        brute.setK(k);
        brute.setSimilarity(similarity);
        HashMap<Node, NeighborList> ground_truth_graph = brute.computeGraph(nodes);
        
        int correct = 0;
        for (Node node : nodes) {            
            correct += graph.get(node).CountCommonValues(ground_truth_graph.get(node));
        }
        
        System.out.println("Computed similarities: " + builder.getComputedSimilarities());
        double speedup_ratio = (double) (nodes.size() * (nodes.size() - 1) / 2) / builder.getComputedSimilarities();
        System.out.println("Speedup ratio: " + speedup_ratio);
        
        double correct_ratio = (double) correct / (nodes.size() * k);
        System.out.println("Correct edges: " + correct + 
                "(" + correct_ratio * 100 + "%)");
        
        System.out.println("Quality-equivalent speedup: " + speedup_ratio * correct_ratio);
    }
    
}
