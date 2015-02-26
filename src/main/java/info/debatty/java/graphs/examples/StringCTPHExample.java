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

import info.debatty.java.graphs.NeighborList;
import info.debatty.java.graphs.Node;
import info.debatty.java.graphs.SimilarityInterface;
import info.debatty.java.graphs.build.Brute;
import info.debatty.java.graphs.build.GraphBuilder;
import info.debatty.java.graphs.build.StringCTPH;
import info.debatty.java.stringsimilarity.QGram;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Thibault Debatty
 */
public class StringCTPHExample {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        // Read nodes
        List<Node<String>> nodes = GraphBuilder.readFile("/home/tibo/Desktop/726-unique-spams");
        int k = 10;
        
        SimilarityInterface<String> similarity = new SimilarityInterface<String>() {
            QGram qg = new QGram(4);

            public double similarity(String v1, String v2) {
                //return JaroWinkler.Similarity((String) n1.value, (String) n2.value);
                return qg.similarity(v1, v2);
            }
        };
        
        
        StringCTPH builder = new StringCTPH();
        builder.setK(k);
        builder.setSimilarity(similarity);
        builder.setNStages(3);
        builder.setNPartitions(30);
        HashMap<Node<String>, NeighborList> graph = builder.computeGraph(nodes);
        
        System.out.println("Estimated speedup: " + builder.estimatedSpeedup());
        
        // Use Brute force to compare results
        Brute brute = new Brute();
        brute.setK(k);
        brute.setSimilarity(similarity);
        HashMap<Node, NeighborList> ground_truth = brute.computeGraph(nodes);
        
        int correct = 0;
        for (Node node : nodes) {            
            correct += graph.get(node).CountCommonValues(ground_truth.get(node));
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
