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
import java.util.Map;

/**
 *
 * @author Thibault Debatty
 * @param <T>
 */
public interface GraphInterface<T> {

    /**
     * Split the graph in connected components (usually you will first prune the
     * graph to remove "weak" edges).
     * @return
     */
    ArrayList<Graph<T>> connectedComponents();

    boolean containsKey(Node node);

    Iterable<Map.Entry<Node<T>, NeighborList>> entrySet();

    /**
     * Get the neighborlist of this node
     * @param node
     * @return the neighborlist of this node
     */
    NeighborList get(Node node);

    int getK();

    SimilarityInterface<T> getSimilarity();

    double getSpeedup();

    /**
     * Remove from the graph all edges with a similarity lower than threshold
     * @param threshold
     */
    void prune(double threshold);

    NeighborList put(Node<T> node, NeighborList neighborlist);

    /**
     * Improved implementation of Graph Nearest Neighbor Search (GNNS) algorithm
     * from paper "Fast Approximate Nearest-Neighbor Search with k-Nearest
     * Neighbor Graph" by Hajebi et al.
     *
     * @param query
     * @param K search K neighbors
     * @return
     */
    NeighborList search(T query, int K);

    /**
     * Improved implementation of Graph Nearest Neighbor Search (GNNS) algorithm
     * from paper "Fast Approximate Nearest-Neighbor Search with k-Nearest
     * Neighbor Graph" by Hajebi et al.
     *
     * The algorithm is basically a best-first search method with random
     * starting points.
     *
     * @param query query point
     * @param K number of neighbors to find (the K from K-nn search)
     * @param expansion (default: 1.01)
     *
     * @return
     */
    NeighborList search(T query, int K, double expansion);

    void setK(int k);

    void setSimilarity(SimilarityInterface<T> similarity);

    void setSpeedup(double speedup);

    int size();

    /**
     * Computes the strongly connected sub-graphs (where every node is reachable
     * from every other node) using Tarjan's algorithm, which has computation
     * cost O(n).
     * @return
     */
    ArrayList<Graph<T>> stronglyConnectedComponents();

    /**
     * Writes the graph as a GEXF file (to be used in Gephi, for example)
     * @param filename
     * @throws FileNotFoundException
     * @throws IOException
     */
    void writeGEXF(String filename) throws FileNotFoundException, IOException;
    
}
