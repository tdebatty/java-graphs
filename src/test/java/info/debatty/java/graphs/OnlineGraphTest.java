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

package info.debatty.java.graphs;

import info.debatty.java.graphs.build.Brute;
import info.debatty.java.graphs.build.GraphBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import junit.framework.TestCase;

/**
 *
 * @author Thibault Debatty
 */
public class OnlineGraphTest extends TestCase {
    
    public OnlineGraphTest(String testName) {
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

    /**
     * Test of addNode method, of class OnlineGraph.
     */
    public void testAddNode() {
        System.out.println("addNode");
        
        int n = 1000;
        int n_test = 100;
        
        SimilarityInterface<Double> similarity = new SimilarityInterface<Double>() {
            
            public double similarity(Double value1, Double value2) {
                return 1.0 / (1 + Math.abs(value1 - value2));
            }
        };
        
        System.out.println("create some random nodes...");
        Random rand = new Random();
        List<Node<Double>> data = new ArrayList<Node<Double>>();
        while (data.size() < n) {
            data.add(new Node<Double>(String.valueOf(data.size()), 100.0 + 100.0 * rand.nextGaussian()));
            data.add(new Node<Double>(String.valueOf(data.size()), 150.0 + 100.0 * rand.nextGaussian()));
            data.add(new Node<Double>(String.valueOf(data.size()), 300.0 + 100.0 * rand.nextGaussian()));
            data.add(new Node<Double>(String.valueOf(data.size()), 200.0 + 50.0 * rand.nextGaussian()));
        }

        System.out.println("compute initial graph...");
        GraphBuilder builder = new Brute<Double>();
        builder.setK(10);
        builder.setSimilarity(similarity);
        OnlineGraph<Double> graph = new OnlineGraph<Double>(builder.computeGraph(data));
        
        System.out.println("add some nodes...");
        for (int i =0; i < n_test; i++) {
            Node<Double> query = new Node<Double>(
                    String.valueOf(graph.size()), 400.0 * rand.nextDouble());
            
            System.out.println(graph.addNode(query));
            
            data.add(query);
        }
        
        assertEquals(n + n_test, graph.size());
        
        System.out.println("compute validation graph...");
        Graph validation_graph = builder.computeGraph(data);
        
        int correct = 0;
        for (Node<Double> node : data) {
            correct += graph.get(node).CountCommons(validation_graph.get(node));
        }
        System.out.println("Found " + correct + " correct edges");
        System.out.printf("= %f \n", 100.0 * correct / (10 * data.size()));
        assertTrue(1.0 * correct / (10 * data.size()) > 0.5);
        
    }
    
}
