/*
 * The MIT License
 *
 * Copyright 2015 tibo.
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
import info.debatty.java.graphs.build.GraphBuilder;
import info.debatty.java.graphs.build.StringSuperBit;
import info.debatty.java.stringsimilarity.Cosine;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author tibo
 */
public class StringSuperBitExample {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Read the nodes from file...
        List<Node<String>> nodes = GraphBuilder.readFile("/home/tibo/Desktop/726-unique-spams");
        
        // Number of edges per node (the k in k-nn graph)
        int k = 10;
        
        // SuperBit is the recommended LSH algorihm when the similarity metric
        // used is cosine similarity
        SimilarityInterface similarity = new SimilarityInterface<String>() {
            Cosine cosine = new Cosine(4);

            public double similarity(String v1, String v2) {
                return cosine.similarity(v1, v2);
            }
        };
        
        // Create and configure graph builder
        // By default, all partitioning graph builders use 
        // Brute force inside the partitions
        StringSuperBit builder = new StringSuperBit();
        builder.setNStages(2);
        builder.setNPartitions(4);
        builder.setShingleSize(4);
        
        // Or we can use any graph builder...
        //NNDescent internal_nndescent = new NNDescent();
        //internal_nndescent.setDelta(0.1);
        //internal_nndescent.setRho(1.0);
        //builder.setInternalBuilder(internal_nndescent);
        
        // Optionnally, get some feedback
        builder.setCallback(new CallbackInterface() {

            public void call(HashMap<String, Object> data) {
                System.out.println(data);
            }
        });
        
        builder.setK(k);
        builder.setSimilarity(similarity);
        
        // Compute graph
        HashMap<Node<String>, NeighborList> graph = builder.computeGraph(nodes);
        
        // Optionnally, we can test the builder
        // This will compute the approximate graph, and then the exact graph
        // and compare results...
        builder.test(nodes);
    }
}
