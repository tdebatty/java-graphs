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
 * Implementation of approximate online graph building algorithm, as presented
 * in "Fast Online k-nn Graph Building" by Debatty et al.
 *
 * @see <a href="http://arxiv.org/abs/1602.06819">Fast Online k-nn Graph
 * Building</a>
 * @author Thibault Debatty
 * @param <T>
 */
public class OnlineGraph<T> implements GraphInterface<T> {

    private final Graph<T> graph;
    private int update_depth;

    protected static final int DEFAULT_UPDATE_DEPTH = 3;
    protected static final double DEFAULT_SEARCH_SPEEDUP = 4.0;

    /**
     * Implementation of approximate online graph building algorithm, as
     * presented in "Fast Online k-nn Graph Building" by Debatty et al.
     *
     * Start with an initial graph.
     *
     * @see <a href="http://arxiv.org/abs/1602.06819">Fast Online k-nn Graph
     * Building</a>
     * @param initial
     */
    public OnlineGraph(final Graph<T> initial) {
        this.graph = initial;
        this.update_depth = DEFAULT_UPDATE_DEPTH;
    }

    /**
     * Implementation of approximate online graph building algorithm, as
     * presented in "Fast Online k-nn Graph Building" by Debatty et al.
     *
     * Start with an empty graph.
     *
     * @see <a href="http://arxiv.org/abs/1602.06819">Fast Online k-nn Graph
     * Building</a>
     * @param k number of edges per node
     */
    public OnlineGraph(final int k) {
        this.graph = new Graph<T>(k);
        this.update_depth = DEFAULT_UPDATE_DEPTH;
    }

    /**
     * Modify the depth for updating existing edges (default is 2).
     * @param update_depth
     */
    public final void setDepth(final int update_depth) {
        this.update_depth = update_depth;
    }

    /**
     * Add a node to the online graph, using a speedup of 4 compared to
     * exhaustive search.
     *
     * @param node
     * @return
     */
    public final int add(final Node<T> node) {
        return add(node, DEFAULT_SEARCH_SPEEDUP);
    }

    /**
     * Add a node to the online graph.
     *
     * @param node
     * @param speedup compared to exhaustive search
     * @return
     */
    public final int add(final Node<T> node, final double speedup) {

        NeighborList neighborlist = graph.search(node.value, graph.k, speedup);
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

        int similarities = (int) (graph.size() / speedup);
        for (int d = 0; d < update_depth; d++) {
            while (!analyze.isEmpty()) {
                Node other = analyze.pop();
                NeighborList other_neighborlist = graph.get(other);

                // Add neighbors to the list of nodes to analyze at
                // next iteration
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
            next_analyze = new LinkedList<Node<T>>();
        }

        return similarities;
    }

    /**
     * Remove a node from the graph (and update the graph) using fast
     * approximate algorithm.
     * @param node_to_remove
     * @return the number of similarities that were computed.
     */
    public final int remove(final Node<T> node_to_remove) {
        // Build the list of nodes to update
        LinkedList<Node<T>> nodes_to_update = new LinkedList<Node<T>>();

        for (Node<T> node : this.graph.getNodes()) {
            NeighborList nl = graph.get(node);
            if (nl.containsNode(node_to_remove)) {
                nodes_to_update.add(node);
                nl.removeNode(node_to_remove);
            }
        }

        // Build the list of candidates
        LinkedList<Node<T>> initial_candidates = new LinkedList<Node<T>>();
        initial_candidates.add(node_to_remove);
        initial_candidates.addAll(nodes_to_update);

        LinkedList<Node<T>> candidates = propagate(initial_candidates);
        while (candidates.contains(node_to_remove)) {
            candidates.remove(node_to_remove);
        }

        // Update the nodes_to_update
        int similarities = 0;
        for (Node<T> node_to_update : nodes_to_update) {
            NeighborList nl_to_update = graph.get(node_to_update);
            for (Node<T> candidate : candidates) {
                similarities++;
                double similarity = graph.similarity.similarity(
                        node_to_update.value,
                        candidate.value);

                nl_to_update.add(new Neighbor(candidate, similarity));
            }
        }

        // Remove node_to_remove
        graph.map.remove(node_to_remove);

        return similarities;

    }

    private LinkedList<Node<T>> propagate(
            final LinkedList<Node<T>> initial_candidates) {
        LinkedList<Node<T>> candidates = new LinkedList<Node<T>>();
        candidates.addAll(initial_candidates);

        // I can NOT loop over candidates as I will add items to it inside the
        // loop!
        for (Node<T> node : initial_candidates) {

            // As depth will be small, I can use recursion here...
            propagate(candidates, node, 0);
        }


        return candidates;
    }

    private void propagate(
            final LinkedList<Node<T>> candidates,
            final Node<T> node,
            final int current_depth) {

        for (Neighbor n : graph.get(node)) {
            if (!candidates.contains(n.node)) {
                candidates.add(n.node);

                if (current_depth < update_depth) {
                    // don't use current_depth++ here as we will reuse it in
                    // the for loop !
                    propagate(candidates, n.node, current_depth + 1);
                }
            }
        }

    }

    public final ArrayList<Graph<T>> connectedComponents() {
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

    public void prune(double threshold) {
        graph.prune(threshold);
    }

    public NeighborList put(Node<T> node, NeighborList neighborlist) {
        return graph.put(node, neighborlist);
    }

    public NeighborList search(T query, int K) {
        return graph.search(query, K);
    }

    public NeighborList search(T query, int K, double speedup) {
        return graph.search(query, K, speedup);
    }

    public NeighborList search(T query, int K, double speedup, double expansion) {
        return graph.search(query, K, speedup, expansion);
    }

    public void setK(int k) {
        graph.setK(k);
    }

    public void setSimilarity(SimilarityInterface<T> similarity) {
        graph.setSimilarity(similarity);
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
            throws InterruptedException, ExecutionException {
        return graph.searchExhaustive(query, K);
    }
}
