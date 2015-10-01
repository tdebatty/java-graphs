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

package info.debatty.java.graphs;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * k-nn graph, represented as a mapping node => neighborlist
 * @author Thibault Debatty
 * @param <T> The type of nodes value
 */
public class Graph<T> extends HashMap<Node<T>, NeighborList> {
            
    public Graph(int n) {
        super(n);
    }
    
    public Graph() {
        super();
    }
    
    /**
     * Get the neighborlist of this node
     * @param node
     * @return the neighborlist of this node 
     */
    public NeighborList get(Node node) {
        return super.get(node);
    }
    
    /**
     * Remove from the graph all edges with a similarity lower than threshold
     * @param threshold 
     */
    public void prune(double threshold) {
        for (NeighborList nl : this.values()) {
            
            // We cannot remove inside the loop
            // => do it in 2 steps:
            ArrayList<Neighbor> to_remove = new ArrayList<Neighbor>();
            for (Neighbor n : nl) {
                if (n.similarity < threshold) {
                    to_remove.add(n);
                }
            }
            
            nl.removeAll(to_remove);
        }
    }
    
    /**
     * Split the graph in connected components (usually you will first prune the
     * graph to remove "weak" edges).
     * @return 
     */
    public ArrayList<Graph<T>> connectedComponents() {
        ArrayList<Graph<T>> subgraphs = new ArrayList<Graph<T>>();
        ArrayList<Node<T>> nodes_to_process = new ArrayList<Node<T>>(this.keySet());
        
        for (int i = 0; i < nodes_to_process.size(); i++) {
            Node n = nodes_to_process.get(i);
            if (n == null) {
                continue;
            }
            Graph<T> subgraph = new Graph<T>();
            subgraphs.add(subgraph);
            
            addAndFollow(subgraph, n, nodes_to_process);
        }
        
        return subgraphs;
    }
    
    private void addAndFollow(Graph<T> subgraph, Node<T> node, ArrayList<Node<T>> nodes_to_process) {
        nodes_to_process.remove(node);
        subgraph.put(node, this.get(node));
        
        for (Neighbor neighbor : this.get(node)) {
            if (! subgraph.containsKey(neighbor.node)) {
                addAndFollow(subgraph, neighbor.node, nodes_to_process);
            }
        }
    }
    
    /**
     *
     * @param Q query point
     * @param K number of neighbors to find
     * @param R number of random restarts
     * @param T number of greedy steps
     * @param E number of expansions
     * @param rho similarity metric
     * @return
     */
    public NeighborList search(Node<T> Q, int K, int R, int T, int E, SimilarityInterface<T> rho, boolean debug) {
        HashMap<Node<T>, Double> visited_nodes = new HashMap<Node<T>, Double>();
        
        int sims = 0;
        Iterator<Node<T>> nodes_iterator = this.keySet().iterator();
        for (int r = 0; r < R; r++) {
            // Select a random node from the graph
            Node<T> Y = nodes_iterator.next();
            if (debug) {
                System.out.println("Starting from node " + Y.id);
            }
            
            for (int t = 0; t < T; t++) {
                Iterator<Neighbor> Y_nl_iterator = this.get(Y).iterator();
                
                Node<T> most_similar_node = null;
                double highest_similarity = -1;
                
                // From Y, pick E neighbors and add them to th list of visited nodes
                for (int e = 0; e < E; e++) {
                    if (! Y_nl_iterator.hasNext()) {
                        break;
                    }
                    
                    Node<T> other_node = Y_nl_iterator.next().node;
                    
                    sims++;
                    double similarity = rho.similarity(Q.value, other_node.value);
                    if (similarity > highest_similarity) {
                        most_similar_node = other_node;
                        highest_similarity = similarity;
                    }
                    
                    if (! visited_nodes.containsKey(other_node)) {
                        visited_nodes.put(other_node, similarity);
                    }
                    
                }
                
                Y = most_similar_node;
                
            }
            
        }
        
        if (debug) {
            System.out.println("Computed similarities: " + sims);
        }
        
        NeighborList neighborList = new NeighborList(K);
        for (Map.Entry<Node<T>, Double> entry : visited_nodes.entrySet()) {
            neighborList.add(new Neighbor(entry.getKey(), entry.getValue()));
        }
        return neighborList;
    }
    
    /**
     * Writes the graph as a GEXF file (to be used in Gephi, for example)
     * @param filename
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public void writeGEXF(String filename) throws FileNotFoundException, IOException {
        Writer out = new OutputStreamWriter(new FileOutputStream(filename));
        out.write(this.gexf_header());
        
        // Write nodes
        out.write("<nodes>\n");
        for (Node node : this.keySet()) {
            out.write("<node id=\"" + node.id + "\" label=\"" + node.id + "\" />\n");
        }
        out.write("</nodes>\n");
            
        // Write edges
        out.write("<edges>\n");
        int i = 0;
        for (Node source : this.keySet()) {
            for (Neighbor target : this.get(source)) {
                out.write("<edge id=\"" + i + "\" source=\"" + source.id + "\" "
                        + "target=\"" + target.node.id + "\" "
                        + "weight=\"" + target.similarity + "\" />\n");
                i++;
            }
        }
            
        out.write("</edges>");
                    
        // End the file
        out.write("</graph>\n" +
                "</gexf>");
        out.close();
    }
    
    private String gexf_header() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<gexf xmlns=\"http://www.gexf.net/1.2draft\" version=\"1.2\">\n" +
            "<meta>\n" +
            "<creator>info.debatty.java.graphs.Graph</creator>\n" +
            "<description></description>\n" +
            "</meta>\n" +
            "<graph mode=\"static\" defaultedgetype=\"directed\">\n";
    }
}
