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
import info.debatty.java.graphs.Node;
import info.debatty.java.graphs.SimilarityInterface;
import java.util.ArrayList;
import java.util.Random;
import static junit.framework.Assert.assertEquals;
import junit.framework.TestCase;

/**
 *
 * @author Thibault Debatty
 */
public class NNDescentTest extends TestCase {
    
    public NNDescentTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testComputeGraph() {
        System.out.println("computeGraph");
        
        int n = 2000;
        int k = 10;
        
        // Generate some nodes
        Random rand = new Random();
        ArrayList<Node<Integer>> nodes = new ArrayList<Node<Integer>>(n);
        for (int i = 0; i < n; i++) {
            nodes.add(new Node<Integer>(String.valueOf(i), rand.nextInt()));
        }
        
        SimilarityInterface<Integer> sim = new SimilarityInterface<Integer>() {
            
            public double similarity(Integer value1, Integer value2) {
                return 1.0 / (1.0 + Math.abs(value1 - value2));
            }
        };
        
        // Instantiate and configure the brute-force graph building algorithm
        NNDescent<Integer> nndes = new NNDescent<Integer>();
        nndes.setK(k);
        nndes.setSimilarity(sim);
        nndes.setDelta(0.1);
        nndes.setMaxIterations(10);
        nndes.setRho(0.6);
        
        // Run the algorithm, and get the resulting neighbor lists
        Graph<Integer> graph = nndes.computeGraph(nodes);
        
        // Test using brute builder
        Brute brute = new Brute();
        brute.setK(k);
        brute.setSimilarity(sim);
        Graph exact_graph = brute.computeGraph(nodes);

        int correct = 0;
        for (Node<Integer> node : nodes) {
            correct += graph.get(node).CountCommonValues(exact_graph.get(node));
        }
        System.out.println("found " + correct + " correct edges");
        System.out.println("" + 100.0 * correct / (n * k) + "%");
        assertTrue((1.0 * correct / (n*k)) > 0.8);
        

    }
    
}
