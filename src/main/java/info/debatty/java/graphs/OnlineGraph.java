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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author Thibault Debatty
 * @param <T>
 */
public class OnlineGraph<T> implements GraphInterface<T> {
    
    protected Graph<T> graph;
    protected int expansion_levels = 3;
    
    public OnlineGraph(Graph<T> initial) {
        this.graph = initial;
                
    }
    
    public OnlineGraph() {
        this.graph = new Graph<T>();
    }
    
    public OnlineGraph(int k) {
        this.graph = new Graph<T>(k);
    }
    
    public int addNode(Node<T> node) {
        
        NeighborList neighborlist = graph.search(node.value, graph.k);
        graph.put(node, neighborlist);
        
        // Nodes to analyze at this iteration
        LinkedList<Node<T>> analyze = new LinkedList<Node<T>>();
        
        // Nodes to analyze at next iteration
        LinkedList<Node<T>> next_analyze = new LinkedList<Node<T>>();
        
        // List of already analyzed nodes
        HashMap<Node<T>, Boolean> visited = new HashMap<Node<T>, Boolean>();
        
        // Fill the list of nodes to analyze
        for (Neighbor neighbor : graph.get(node)) {
            analyze.add(neighbor.node);
        }
        
        int similarities = (int) (graph.size() / graph.speedup);
        for (int level = 0; level < expansion_levels; level++) {
            while (!analyze.isEmpty()){
                Node other = analyze.pop();
                NeighborList other_neighborlist = graph.get(other);
                
                // Add neighbors to the list of nodes to analyze at next iteration
                for (Neighbor other_neighbor : other_neighborlist) {
                    if (!visited.containsKey(other_neighbor.node)) {
                        next_analyze.add(other_neighbor.node);
                    }
                }
                
                // Try to add the new node (if sufficiently similar)
                similarities++;
                other_neighborlist.add(new Neighbor(
                        node,
                        graph.similarity.similarity(
                                node.value,
                                (T) other.value)));
                
                visited.put(other, Boolean.TRUE);
            }
            
            analyze = next_analyze;
            next_analyze =  new LinkedList<Node<T>>();
        }
        
        return similarities;
    }

    public ArrayList<Graph<T>> connectedComponents() {
        return graph.connectedComponents();
    }

    public boolean containsKey(Node node) {
        return graph.containsKey(node);
    }

    public Iterable<Map.Entry<Node<T>, NeighborList>> entrySet() {
        return graph.entrySet();
    }

    public NeighborList get(Node node) {
        return graph.get(node);
    }

    public int getK() {
        return graph.getK();
    }

    public SimilarityInterface<T> getSimilarity() {
        return graph.getSimilarity();
    }

    public double getSpeedup() {
        return graph.getSpeedup();
    }

    public void prune(double threshold) {
        graph.prune(threshold);
    }

    public NeighborList put(Node<T> node, NeighborList neighborlist) {
        return graph.put(node, neighborlist);
    }

    public NeighborList search(T query, int K) {
        return graph.search(query, K);
    }

    public NeighborList search(T query, int K, double expansion) {
        return graph.search(query, K, expansion);
    }

    public void setK(int k) {
        graph.setK(k);
    }

    public void setSimilarity(SimilarityInterface<T> similarity) {
        graph.setSimilarity(similarity);
    }

    public void setSpeedup(double speedup) {
        graph.setSpeedup(speedup);
    }

    public int size() {
        return graph.size();
    }

    public ArrayList<Graph<T>> stronglyConnectedComponents() {
        return graph.stronglyConnectedComponents();
    }

    public void writeGEXF(String filename) throws FileNotFoundException, IOException {
        graph.writeGEXF(filename);
    }

    public Iterable<Node<T>> getNodes() {
        return graph.getNodes();
    }

    public NeighborList searchExhaustive(T query, int K) 
            throws InterruptedException,ExecutionException{
        return graph.searchExhaustive(query, K);
    }
}
