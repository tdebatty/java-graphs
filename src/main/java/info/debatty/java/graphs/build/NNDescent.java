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

import info.debatty.java.graphs.Edge;
import info.debatty.java.graphs.Graph;
import info.debatty.java.graphs.Neighbor;
import info.debatty.java.graphs.NeighborList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of NN-Descent k-nn graph building algorithm. Based on the
 * paper "Efficient K-Nearest Neighbor Graph Construction for Generic Similarity
 * Measures" by Dong et al. http://www.cs.princeton.edu/cass/papers/www11.pdf
 *
 * NN-Descent works by iteratively exploring the neighbors of neighbors... It is
 * not suitable for small datasets (less than 500 items)!
 *
 * @author Thibault Debatty
 * @param <T> The type of nodes value
 */
public class NNDescent<T> extends GraphBuilder<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(
            NNDescent.class);

    protected double rho = 0.5; // Standard : 1, Fast: 0.5
    protected double delta = 0.001;
    protected int max_iterations = Integer.MAX_VALUE;

    protected int iterations = 0;
    protected int c;

    /**
     * Contains the list of neighbors that have been processed. Has we use a
     * hashset, we have to use edges (which contain a reference to the source
     * node) instead of neighbors for the concrete implementation.
     */
    protected Set<Edge> processed;

    /**
     * Get the number of edges modified at the last iteration
     *
     * @return
     */
    public int getC() {
        return c;
    }

    /**
     * Get the number of executed iterations
     *
     * @return
     */
    public int getIterations() {
        return iterations;
    }

    public double getRho() {
        return rho;
    }

    /**
     * Sampling coefficient. In interval ]0, 1.0] Typical value for fast
     * computation is 0.5 Use 1.0 for precise computation Default is 0.5
     *
     * @param rho
     */
    public void setRho(double rho) {
        if (rho > 1.0 || rho <= 0.0) {
            throw new IllegalArgumentException("0 < rho <= 1.0");
        }
        this.rho = rho;
    }

    public double getDelta() {
        return delta;
    }

    /**
     * Early termination coefficient. The algorithm stops when less than this
     * proportion of edges are modified Should be in ]0, 1.0[ Default is 0.001
     *
     * @param delta
     */
    public void setDelta(double delta) {
        if (rho >= 1.0 || rho <= 0.0) {
            throw new IllegalArgumentException("0 < delta < 1.0");
        }
        this.delta = delta;
    }

    public int getMaxIterations() {
        return max_iterations;
    }

    /**
     * Set the maximum number of iterations Default is no max
     * (Integer.MAX_VALUE)
     *
     * @param max_iterations
     */
    public void setMaxIterations(int max_iterations) {
        if (max_iterations < 0) {
            throw new IllegalArgumentException(
                    "max_iterations should be positive!");
        }
        this.max_iterations = max_iterations;
    }

    @Override
    protected Graph<T> _computeGraph(List<T> nodes) {

        if (nodes.size() < 2 * k) {
            LOGGER.warn("NNDescent should be used for large graphs!");
        }

        iterations = 0;
        processed = new HashSet<Edge>(nodes.size() * k);

        if (nodes.size() <= (k + 1)) {
            return MakeFullyLinked(nodes);
        }

        Graph<T> neighborlists = new Graph<T>(nodes.size());
        HashMap<T, ArrayList<T>> old_lists, new_lists, old_lists_2, new_lists_2;

        old_lists = new HashMap<T, ArrayList<T>>(nodes.size());
        new_lists = new HashMap<T, ArrayList<T>>(nodes.size());

        HashMap<String, Object> data = new HashMap<String, Object>();

        // B[v]←− Sample(V,K)×{?∞, true?} ∀v ∈ V
        // For each node, create a random neighborlist
        for (T v : nodes) {
            neighborlists.put(v, RandomNeighborList(nodes, v));
        }

        // loop
        while (true) {
            iterations++;
            c = 0;

            // for v ∈ V do
            // old[v]←− all items in B[v] with a false flag
            // new[v]←− ρK items in B[v] with a true flag
            // Mark sampled items in B[v] as false;
            for (int i = 0; i < nodes.size(); i++) {
                T v = nodes.get(i);
                old_lists.put(v, PickFalses(v, neighborlists.getNeighbors(v)));
                new_lists.put(v, PickTruesAndMark(v, neighborlists.getNeighbors(v)));

            }

            // old′ ←Reverse(old)
            // new′ ←Reverse(new)
            old_lists_2 = Reverse(nodes, old_lists);
            new_lists_2 = Reverse(nodes, new_lists);

            // for v ∈ V do
            for (int i = 0; i < nodes.size(); i++) {
                T v = nodes.get(i);
                // old[v]←− old[v] ∪ Sample(old′[v], ρK)
                // new[v]←− new[v] ∪ Sample(new′[v], ρK)
                old_lists.put(v, Union(old_lists.get(v), Sample(old_lists_2.get(v), (int) (rho * k))));
                new_lists.put(v, Union(new_lists.get(v), Sample(new_lists_2.get(v), (int) (rho * k))));

                // for u1,u2 ∈ new[v], u1 < u2 do
                for (int j = 0; j < new_lists.get(v).size(); j++) {
                    T u1 = new_lists.get(v).get(j);

                    //int u1_i = Find(u1); // position of u1 in nodes
                    for (int l = j + 1; l < new_lists.get(u1).size(); l++) {
                        T u2 = new_lists.get(u1).get(l);
                            //int u2_i = Find(u2);

                        // l←− σ(u1,u2)
                        // c←− c+UpdateNN(B[u1], u2, l, true)
                        // c←− c+UpdateNN(B[u2], u1, l, true)
                        double s = Similarity(u1, u2);
                        c += UpdateNL(neighborlists.getNeighbors(u1), u2, s);
                        c += UpdateNL(neighborlists.getNeighbors(u2), u1, s);
                    }

                    // or u1 ∈ new[v], u2 ∈ old[v] do
                    for (int l = 0; l < old_lists.get(v).size(); l++) {
                        T u2 = old_lists.get(v).get(l);

                        if (u1.equals(u2)) {
                            continue;
                        }

                        //int u2_i = Find(u2);
                        double s = Similarity(u1, u2);
                        c += UpdateNL(neighborlists.getNeighbors(u1), u2, s);
                        c += UpdateNL(neighborlists.getNeighbors(u2), u1, s);
                    }
                }
            }

            //System.out.println("C : " + c);
            if (callback != null) {
                data.put("c", c);
                data.put("computed_similarities", computed_similarities);
                data.put("computed_similarities_ratio",
                        (double) computed_similarities / (nodes.size() * (nodes.size() - 1) / 2));
                data.put("iterations", iterations);

                callback.call(data);
            }

            if (c <= (delta * nodes.size() * k)) {
                break;
            }

            if (iterations >= max_iterations) {
                break;
            }
        }

        return neighborlists;
    }

    protected ArrayList<T> Union(ArrayList<T> l1, ArrayList<T> l2) {
        ArrayList<T> r = new ArrayList<T>();
        for (T n : l1) {
            if (!r.contains(n)) {
                r.add(n);
            }
        }

        for (T n : l2) {
            if (!r.contains(n)) {
                r.add(n);
            }
        }

        return r;
    }

    protected NeighborList RandomNeighborList(List<T> nodes, T for_node) {
        //System.out.println("Random NL for node " + for_node);
        NeighborList nl = new NeighborList(k);
        Random r = new Random();

        while (nl.size() < k) {
            T node = nodes.get(r.nextInt(nodes.size()));
            if (!node.equals(for_node)) {
                double s = Similarity(node, for_node);
                nl.add(new Neighbor(node, s));
            }
        }

        return nl;
    }

    protected ArrayList<T> PickFalses(T node, NeighborList neighborList) {
        ArrayList<T> falses = new ArrayList<T>();
        for (Neighbor<T> n : neighborList) {
            Edge edge = new Edge(node, n);
            if (processed.contains(edge)) {
                falses.add(n.node);
            }
        }

        return falses;
    }

    /**
     * pick new neighbors with a probability of rho, and mark them as false
     *
     * @param neighborList
     * @return
     */
    protected ArrayList<T> PickTruesAndMark(T node, NeighborList neighborList) {
        ArrayList<T> r = new ArrayList<T>();
        for (Neighbor<T> n : neighborList) {
            Edge<T> edge = new Edge<T>(node, n);
            if (!processed.contains(edge) && Math.random() < rho) {
                processed.add(edge);
                r.add(n.node);
            }
        }

        return r;
    }

    protected HashMap<T, ArrayList<T>> Reverse(List<T> nodes, Map<T, ArrayList<T>> lists) {

        HashMap<T, ArrayList<T>> R = new HashMap<T, ArrayList<T>>(nodes.size());

        // Create all arraylists
        for (T n : nodes) {
            R.put(n, new ArrayList<T>());
        }

        // For each node and corresponding arraylist
        for (T node : nodes) {
            ArrayList<T> list = lists.get(node);
            for (T other_node : list) {
                R.get(other_node).add(node);
            }
        }

        return R;
    }

    /**
     * Reverse NN array R[v] is the list of elements (u) for which v is a
     * neighbor (v is in B[u])
     *
     * @param nodes
     * @param count
     * @return
     */
    protected ArrayList<T> Sample(ArrayList<T> nodes, int count) {
        Random r = new Random();
        while (nodes.size() > count) {
            nodes.remove(r.nextInt(nodes.size()));
        }

        return nodes;

    }

    protected int UpdateNL(NeighborList nl, T n, double similarity) {
        Neighbor neighbor = new Neighbor(n, similarity);
        return nl.add(neighbor) ? 1 : 0;
    }

    protected double Similarity(T n1, T n2) {
        computed_similarities++;
        return similarity.similarity(n1, n2);

    }

    protected Graph<T> MakeFullyLinked(List<T> nodes) {
        Graph<T> neighborlists = new Graph<T>(nodes.size());
        for (T node : nodes) {
            NeighborList neighborlist = new NeighborList(k);
            for (T other_node : nodes) {
                if (node.equals(other_node)) {
                    continue;
                }

                neighborlist.add(new Neighbor(
                        other_node,
                        Similarity(node, other_node)
                ));
            }
            neighborlists.put(node, neighborlist);
        }

        return neighborlists;
    }
}
