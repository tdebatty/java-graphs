/*
 * The MIT License
 *
 * Copyright 2017 Thibault Debatty.
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
import info.debatty.java.graphs.SimilarityInterface;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thibault Debatty
 * @param <T> The type of nodes
 */
abstract class AbstractNNDescent<T> extends GraphBuilder<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(
            AbstractNNDescent.class);

    private static final int MIN_SIZE = 500;
    private static final double DEFAULT_RHO = 0.5;
    private static final double DEFAULT_DELTA = 0.001;

    // Parameters
    private double rho = DEFAULT_RHO; // Standard : 1, Fast: 0.5
    private double delta = DEFAULT_DELTA;
    private int max_iterations = Integer.MAX_VALUE;

    // State
    // similarity is used by some of the helper functions
    private SimilarityInterface<T> similarity;

    /**
     * Contains the list of neighbors that have been processed. Has we use a
     * hashset, we have to use edges (which contain a reference to the source
     * node) instead of neighbors for the concrete implementation.
     */
    private Set<Edge> processed;

    /**
     *
     * @return
     */
    public final double getRho() {
        return rho;
    }

    /**
     * Sampling coefficient. In interval ]0, 1.0] Typical value for fast
     * computation is 0.5 Use 1.0 for precise computation Default is 0.5
     *
     * @param rho
     */
    public final void setRho(final double rho) {
        if (rho > 1.0 || rho <= 0.0) {
            throw new IllegalArgumentException("0 < rho <= 1.0");
        }
        this.rho = rho;
    }

    /**
     *
     * @return
     */
    public final double getDelta() {
        return delta;
    }

    /**
     * Early termination coefficient. The algorithm stops when less than this
     * proportion of edges are modified Should be in ]0, 1.0[ Default is 0.001
     *
     * @param delta
     */
    public final void setDelta(final double delta) {
        if (rho >= 1.0 || rho <= 0.0) {
            throw new IllegalArgumentException("0 < delta < 1.0");
        }
        this.delta = delta;
    }

    /**
     *
     * @return
     */
    public final int getMaxIterations() {
        return max_iterations;
    }

    /**
     * Set the maximum number of iterations. Default is
     * Integer.MAX_VALUE
     *
     * @param max_iterations
     */
    public final void setMaxIterations(final int max_iterations) {
        if (max_iterations < 0) {
            throw new IllegalArgumentException(
                    "max_iterations must be positive!");
        }
        this.max_iterations = max_iterations;
    }

    protected final ArrayList<T> union(
            final ArrayList<T> l1,
            final ArrayList<T> l2) {

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

    protected final NeighborList randomNeighborList(
            final List<T> nodes, final T for_node) {

        NeighborList nl = new NeighborList(getK());
        Random r = new Random();

        while (nl.size() < getK()) {
            T node = nodes.get(r.nextInt(nodes.size()));
            if (!node.equals(for_node)) {
                double s = similarity.similarity(node, for_node);
                nl.add(new Neighbor(node, s));
            }
        }

        return nl;
    }

    protected final ArrayList<T> pickFalses(
            final T node, final NeighborList neighbor_list) {
        ArrayList<T> falses = new ArrayList<T>();
        for (Neighbor<T> n : neighbor_list) {
            Edge edge = new Edge(node, n);
            if (processed.contains(edge)) {
                falses.add(n.getNode());
            }
        }

        return falses;
    }

    /**
     * Pick new neighbors with a probability of rho, and mark them as false.
     *
     * @param node
     * @param neighbor_list
     * @return
     */
    protected final ArrayList<T> pickTruesAndMark(
            final T node, final NeighborList neighbor_list) {
        ArrayList<T> r = new ArrayList<T>();
        for (Neighbor<T> n : neighbor_list) {
            Edge<T> edge = new Edge<T>(node, n);
            if (!processed.contains(edge) && Math.random() < rho) {
                processed.add(edge);
                r.add(n.getNode());
            }
        }

        return r;
    }

    /**
     * Reverse NN array R[v] is the list of elements (u) for which v is a
     * neighbor (v is in B[u]).
     *
     * @param nodes
     * @param lists
     * @return
     */
    protected final HashMap<T, ArrayList<T>> reverse(
            final List<T> nodes, final Map<T, ArrayList<T>> lists) {

        HashMap<T, ArrayList<T>> reverse =
                new HashMap<T, ArrayList<T>>(nodes.size());

        // Create all arraylists
        for (T n : nodes) {
            reverse.put(n, new ArrayList<T>());
        }

        // For each node and corresponding arraylist
        for (T node : nodes) {
            ArrayList<T> list = lists.get(node);
            for (T other_node : list) {
                reverse.get(other_node).add(node);
            }
        }

        return reverse;
    }


    protected final ArrayList<T> sample(
            final ArrayList<T> nodes, final int count) {
        Random r = new Random();
        while (nodes.size() > count) {
            nodes.remove(r.nextInt(nodes.size()));
        }

        return nodes;
    }

    /**
     *
     * @param nl
     * @param n
     * @param similarity
     * @return
     */
    protected final int updateNL(
            final NeighborList nl,
            final T n,
            final double similarity) {

        Neighbor neighbor = new Neighbor(n, similarity);
        if (nl.add(neighbor)) {
            return 1;
        }

        return 0;
    }


    private Graph<T> makeFullyLinked(final List<T> nodes) {
        Graph<T> neighborlists = new Graph<T>(nodes.size());
        for (T node : nodes) {
            NeighborList neighborlist = new NeighborList(getK());
            for (T other_node : nodes) {
                if (node.equals(other_node)) {
                    continue;
                }

                neighborlist.add(new Neighbor(
                        other_node,
                        similarity.similarity(node, other_node)
                ));
            }
            neighborlists.put(node, neighborlist);
        }

        return neighborlists;
    }

    @Override
    protected Graph<T> computeGraph(
            final List<T> nodes,
            final int k,
            final SimilarityInterface<T> similarity) {

        if (nodes.size() < MIN_SIZE) {
            LOGGER.warn("NNDescent should be used for large graphs!");
        }

        this.similarity = similarity;

        if (nodes.size() <= (k + 1)) {
            return makeFullyLinked(nodes);
        }

        this.processed = getSetInstance(nodes.size() * k);

        return nndescent(nodes, similarity);
    }

    abstract Graph<T> nndescent(
            List<T> nodes, SimilarityInterface<T> similarity);

    /**
     * Return the correct instance of Set<Edge>. Must be synchronized for
     * multi-threaded implementation of NNDescent.
     * @param size
     * @return
     */
    protected abstract Set<Edge> getSetInstance(int size);

}
