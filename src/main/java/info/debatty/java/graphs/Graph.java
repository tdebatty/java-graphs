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

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * k-nn graph, represented as a mapping node => neighborlist.
 *
 * @author Thibault Debatty
 * @param <T> The class of the node, used when getting a node
 */
public class Graph<T> implements Serializable {

    /**
     * Number of edges per node.
     */
    public static final int DEFAULT_K = 10;

    private final HashMap<T, NeighborList> map;
    private SimilarityInterface<T> similarity;
    private int k = DEFAULT_K;

    // These fields are not serialized...
    // drawback: must be very carefull when using/modifying
    private final transient Random rand = new Random();
    // The nodes arraylist is a cache for map.keys()
    // used for example to get a single random node
    private transient ArrayList<T> nodes;

    /**
     * Copy constructor.
     *
     * @param origin
     */
    public Graph(final Graph<T> origin) {
        this.k = origin.k;
        this.similarity = origin.similarity;
        this.map = new HashMap<T, NeighborList>(origin.size());

        for (T node : origin.getNodes()) {
            this.map.put(node, new NeighborList(origin.getNeighbors(node)));
        }
    }

    /**
     * Initialize an empty graph, and set k (number of edges per node).
     * Default k is 10.
     * @param k
     */
    public Graph(final int k) {
        this.k = k;
        this.map = new HashMap<T, NeighborList>();
    }

    /**
     * Initialize an empty graph with k = 10.
     */
    public Graph() {
        this.map = new HashMap<T, NeighborList>();
    }

    /**
     * Get the similarity measure.
     * @return
     */
    public final SimilarityInterface<T> getSimilarity() {
        return similarity;
    }

    /**
     * Set the similarity measure used to build or search the graph.
     * @param similarity
     */
    public final void setSimilarity(final SimilarityInterface<T> similarity) {
        this.similarity = similarity;
    }

    /**
     * Get k (the number of edges per node).
     * @return
     */
    public final int getK() {
        return k;
    }

    /**
     * Set k (the number of edges per node).
     * The existing graph will not be modified.
     * @param k
     */
    public final void setK(final int k) {
        this.k = k;
    }

    /**
     * Get the neighborlist of this node.
     *
     * @param node
     * @return the neighborlist of this node
     */
    public final NeighborList getNeighbors(final T node) {
        return map.get(node);
    }

    /**
     * Get the first node in the graph.
     *
     * @return The first node in the graph
     * @throws NoSuchElementException if the graph is empty...
     */
    public final T first() throws NoSuchElementException {
        return this.getNodes().iterator().next();
    }

    /**
     * Return a random node from the graph.
     * @return
     */
    public final T getRandomNode() {


        return getNodes().get(rand.nextInt(getNodes().size()));
    }

    /**
     * Remove from the graph all edges with a similarity lower than threshold.
     *
     * @param threshold
     */
    public final void prune(final double threshold) {

        for (NeighborList nl : map.values()) {
             nl.prune(threshold);
        }
    }

    /**
     * Split the graph in connected components (usually you will first prune the
     * graph to remove "weak" edges).
     *
     * @return
     */
    public final ArrayList<Graph<T>> connectedComponents() {

        ArrayList<Graph<T>> subgraphs = new ArrayList<Graph<T>>();
        LinkedList<T> nodes_to_process =
                new LinkedList<T>(map.keySet());

        while (!nodes_to_process.isEmpty()) {
            T n = nodes_to_process.peek();
            if (n == null) {
                continue;
            }
            Graph<T> subgraph = new Graph<T>();
            subgraphs.add(subgraph);

            addAndFollow(subgraph, n, nodes_to_process);
        }

        return subgraphs;
    }

    private void addAndFollow(
            final Graph<T> subgraph,
            final T node,
            final LinkedList<T> nodes_to_process) {

        nodes_to_process.remove(node);

        NeighborList neighborlist = this.getNeighbors(node);
        subgraph.put(node, neighborlist);

        if (neighborlist == null) {
            return;
        }

        for (Neighbor<T> neighbor : this.getNeighbors(node)) {
            if (!subgraph.containsKey(neighbor.getNode())) {
                addAndFollow(subgraph, neighbor.getNode(), nodes_to_process);
            }
        }
    }

    /**
     * Computes the strongly connected sub-graphs (where every node is reachable
     * from every other node) using Tarjan's algorithm, which has computation
     * cost O(n).
     *
     * @return
     */
    public final ArrayList<Graph<T>> stronglyConnectedComponents() {

        Stack<NodeParent> explored_nodes = new Stack<NodeParent>();
        Index index = new Index();
        HashMap<T, NodeProperty> bookkeeping =
                new HashMap<T, NodeProperty>(map.size());

        ArrayList<Graph<T>> connected_components = new ArrayList<Graph<T>>();

        for (T n : map.keySet()) {

            if (bookkeeping.containsKey(n)) {
                // This node was already processed...
                continue;
            }

            ArrayList<T> connected_component =
                    this.strongConnect(n, explored_nodes, index, bookkeeping);

            if (connected_component == null) {
                continue;
            }

            // We found a connected component
            Graph<T> subgraph = new Graph<T>(connected_component.size());
            for (T node : connected_component) {
                subgraph.put(node, this.getNeighbors(node));
            }
            connected_components.add(subgraph);

        }

        return connected_components;
    }

    /**
     *
     * @param starting_point
     * @param explored_nodes
     * connected component.
     * @param index
     * @param bookkeeping
     * @return
     */
    private ArrayList<T> strongConnect(
            final T starting_point,
            final Stack<NodeParent> explored_nodes,
            final Index index,
            final HashMap<T, NodeProperty> bookkeeping) {


        // explored_nodes stores the history of nodes explored but not yet
        // assigned to a strongly connected component

        // use a stack to perform depth first search (DFS) without using
        // recursion
        final Stack<NodeParent> nodes_to_process = new Stack<NodeParent>();
        nodes_to_process.push(new NodeParent(starting_point, null));


        while (!nodes_to_process.empty()) {
            NodeParent node_and_parent = nodes_to_process.pop();
            T node = node_and_parent.node;

            bookkeeping.put(
                    node,
                    new NodeProperty(index.value(), index.value()));
            index.inc();
            explored_nodes.add(node_and_parent);

            // process neighbors of this node
            for (Neighbor<T> neighbor : this.getNeighbors(node)) {
                T neighbor_node = neighbor.getNode();

                if (!this.containsKey(neighbor_node)
                        || this.getNeighbors(neighbor_node) == null) {
                    // neighbor_node is actually part of another subgraph
                    // (this can happen during distributed processing)
                    // => skip
                    continue;
                }

                if (bookkeeping.containsKey(neighbor_node)) {
                    // this node was already processed
                    continue;
                }

                boolean skip = false;
                for (NodeParent node_in_queue : nodes_to_process) {
                    // Already in the queue for processing
                    if (node_in_queue.node.equals(neighbor_node)) {
                        skip = true;
                        break;
                    }
                }
                if (skip) {
                    continue;
                }

                // Perform depth first search...
                nodes_to_process.push(new NodeParent(neighbor_node, node));
            }
        }

        // Traverse the stack of explored nodes to update the lowlink value of
        // each node
        for (NodeParent node_and_parent : explored_nodes) {
            T node = node_and_parent.parent;
            T child = node_and_parent.node;

            if (node == null) {
                // this child is actually the starting point.
                continue;
            }

            bookkeeping.get(node).lowlink = Math.min(
                        bookkeeping.get(node).lowlink,
                        bookkeeping.get(child).lowlink);
        }

        for (NodeParent node_and_parent : explored_nodes) {
            T node = node_and_parent.node;
            if (bookkeeping.get(node).lowlink == bookkeeping.get(node).index) {
                // node is the root of a strongly connected component
                // => fetch and return all nodes in this component
                ArrayList<T> connected_component = new ArrayList<T>();

                T other_node;
                do {
                    other_node = explored_nodes.pop().node;
                    bookkeeping.get(other_node).onstack = false;
                    connected_component.add(other_node);
                } while (!starting_point.equals(other_node));

                return connected_component;
            }
        }

        return null;
    }

    /**
     * Store the node and it's parent.
     */
    private class NodeParent {
        private T node;
        private T parent;

        NodeParent(final T node, final T parent) {
            this.node = node;
            this.parent = parent;
        }
    }

    /**
     * Helper class to compute strongly connected components.
     */
    private static class Index {

        private int value;

        public int value() {
            return this.value;
        }

        public void inc() {
            this.value++;
        }
    }

    /**
     * Helper class to compute strongly connected components.
     */
    private static class NodeProperty {

        private int index;
        private int lowlink;
        private boolean onstack;

        NodeProperty(final int index, final int lowlink) {
            this.index = index;
            this.lowlink = lowlink;
            this.onstack = true;
        }
    };

    /**
     *
     * @param node
     * @param neighborlist
     * @return
     */
    public final NeighborList put(
            final T node, final NeighborList neighborlist) {
        return map.put(node, neighborlist);
    }

    /**
     *
     * @param node
     * @return
     */
    public final boolean containsKey(final T node) {
        return map.containsKey(node);
    }

    /**
     *
     * @return
     */
    public final int size() {
        return map.size();
    }

    /**
     *
     * @return
     */
    public final Iterable<Map.Entry<T, NeighborList>> entrySet() {
        return map.entrySet();
    }

    /**
     *
     * @return
     */
    public final ArrayList<T> getNodes() {
        if (nodes == null) {
            nodes = new ArrayList<T>(map.keySet());
        }

        return nodes;
    }

    /**
     * Recursively search neighbors of neighbors, up to a given depth.
     * @param starting_points
     * @param depth
     * @return
     */
    public final LinkedList<T> findNeighbors(
            final LinkedList<T> starting_points,
            final int depth) {
        LinkedList<T> neighbors = new LinkedList<T>();
        neighbors.addAll(starting_points);

        // I can NOT loop over candidates as I will add items to it inside the
        // loop!
        for (T start_node : starting_points) {

            // As depth will be small, I can use recursion here...
            findNeighbors(neighbors, start_node, depth);
        }


        return neighbors;
    }

    private void findNeighbors(
            final LinkedList<T> candidates,
            final T node,
            final int current_depth) {

        // With the distributed online algorithm, the nl might be null
        // because it is located on another partition
        NeighborList nl = getNeighbors(node);
        if (nl == null) {
            return;
        }

        for (Neighbor<T> n : nl) {
            if (!candidates.contains(n.getNode())) {
                candidates.add(n.getNode());

                if (current_depth > 0) {
                    // don't use current_depth++ here as we will reuse it in
                    // the for loop !
                    findNeighbors(candidates, n.getNode(), current_depth - 1);
                }
            }
        }

    }

    /**
     * Get the underlying hash map that stores the nodes and associated
     * neighborlists.
     * @return
     */
    public final HashMap<T, NeighborList> getHashMap() {
        return map;
    }

    /**
     * Multi-thread exhaustive search.
     * @param query
     * @param k
     * @return
     * @throws InterruptedException if thread is interrupted
     * @throws ExecutionException if thread cannot complete
     */
    public final NeighborList search(final T query, final int k)
            throws InterruptedException, ExecutionException {

        int procs = Runtime.getRuntime().availableProcessors();
        ExecutorService pool = Executors.newFixedThreadPool(procs);
        List<Future<NeighborList>> results = new ArrayList();

        for (int i = 0; i < procs; i++) {
            int start = getNodes().size() / procs * i;
            int stop = Math.min(
                    getNodes().size() / procs * (i + 1), getNodes().size());

            results.add(pool.submit(new SearchTask(nodes, query, start, stop)));
        }

        // Reduce
        NeighborList neighbors = new NeighborList(k);
        for (Future<NeighborList> future : results) {
            neighbors.addAll(future.get());
        }
        pool.shutdown();
        return neighbors;
    }

    /**
     * Class used for multi-thread search.
     */
    private class SearchTask implements Callable<NeighborList> {

        private final ArrayList<T> nodes;
        private final T query;
        private final int start;
        private final int stop;

        SearchTask(
                final ArrayList<T> nodes,
                final T query,
                final int start,
                final int stop) {

            this.nodes = nodes;
            this.query = query;
            this.start = start;
            this.stop = stop;
        }

        public NeighborList call() throws Exception {
            NeighborList nl = new NeighborList(k);
            for (int i = start; i < stop; i++) {
                T other = getNodes().get(i);
                nl.add(new Neighbor(
                        other,
                        similarity.similarity(query, other)));
            }
            return nl;

        }
    }

    /**
     * Approximate fast graph based search, as published in "Fast Online k-nn
     * Graph Building" by Debatty et al.
     *
     * @see <a href="http://arxiv.org/abs/1602.06819">Fast Online k-nn Graph
     * Building</a>
     * @param query
     * @return
     */
    public final FastSearchResult fastSearch(final T query) {
        return fastSearch(query, FastSearchConfig.getDefault());
    }

    /**
     * Approximate fast graph based search, as published in "Fast Online k-nn
     * Graph Building" by Debatty et al.
     *
     * @see <a href="http://arxiv.org/abs/1602.06819">Fast Online k-nn Graph
     * Building</a>
     *
     * @param query
     * @param conf
     * @return
     */
    public final FastSearchResult fastSearch(
            final T query,
            final FastSearchConfig conf) {

        return fastSearch(query, conf, getRandomNode());
    }

    /**
     * Approximate fast graph based search, as published in "Fast Online k-nn
     * Graph Building" by Debatty et al.
     *
     * @param conf
     * @param start starting point
     * @see <a href="http://arxiv.org/abs/1602.06819">Fast Online k-nn Graph
     * Building</a>
     * @param query query point
     *
     * @return
     */
    public final FastSearchResult fastSearch(
            final T query,
            final FastSearchConfig conf,
            final T start) {

        FastSearchResult result = new FastSearchResult(conf.getK());
        int max_similarities = (int) (map.size() / conf.getSpeedup());

        // Looking for more nodes than this graph contains...
        // Or fall back to exhaustive search
        if (conf.getK() >= map.size()
                || max_similarities >= map.size()) {

            for (T node : map.keySet()) {
                result.getNeighbors().add(
                        new Neighbor(
                                node,
                                similarity.similarity(
                                        query,
                                        node)));
                result.incSimilarities();
            }
            return result;
        }

        T current_node = start;
        // Node => Similarity with query node
        // Max number of nodes we will visit is max_similarities
        HashMap<T, Double> visited_nodes = new HashMap<T, Double>(
                max_similarities);
        double highest_similarity = 0;

        while (result.getSimilarities() < max_similarities) { // Restart...

            restart : {
                // Already been here => restart
                if (visited_nodes.containsKey(current_node)) {
                    break restart;
                }

                // starting point too far (similarity too small) => restart!
                double current_node_similarity = similarity.similarity(
                        query,
                        current_node);
                result.incSimilarities();

                if (current_node_similarity
                        < highest_similarity / conf.getExpansion()) {

                    break restart;
                }

                // Follow the chain of neighbors
                while (result.getSimilarities() < max_similarities) {

                    NeighborList nl = this.getNeighbors(current_node);

                    // Node has no neighbor (cross partition edge) => restart or
                    // return
                    if (nl == null) {
                        if (conf.isRestartAtBoundary()) {
                            result.incBoundaryRestarts();
                            break restart;

                        } else {
                            result.setBoundaryNode(current_node);
                            return result;
                        }
                    }

                    T best_neighbor = null;
                    double best_neighbor_similarity = current_node_similarity;

                    for (int i = 0; i < conf.getLongJumps(); i++) {
                        // Check a random node (to simulate long jumps)
                        T neighbor = getRandomNode();

                        // Already been here => skip
                        if (visited_nodes.containsKey(neighbor)) {
                            continue;
                        }

                        // Compute similarity to query
                        double neighbor_similarity = similarity.similarity(
                                query,
                                neighbor);
                        result.incSimilarities();
                        visited_nodes.put(neighbor, neighbor_similarity);

                        // If this node provides an improved similarity, keep it
                        if (neighbor_similarity > current_node_similarity) {
                            best_neighbor = neighbor;
                            best_neighbor_similarity = neighbor_similarity;

                            // early break
                            break;
                        }
                    }

                    // Check the neighbors of current_node and try to find a
                    // node with higher similarity
                    Iterator<Neighbor> y_nl_iterator = nl.iterator();
                    while (y_nl_iterator.hasNext()) {

                        T neighbor = (T) y_nl_iterator.next().getNode();

                        if (visited_nodes.containsKey(neighbor)) {
                            continue;
                        }

                        // Compute similarity with query
                        double neighbor_similarity = similarity.similarity(
                                query,
                                neighbor);
                        result.incSimilarities();
                        visited_nodes.put(neighbor, neighbor_similarity);

                        // If this node provides an improved similarity, keep it
                        if (neighbor_similarity > best_neighbor_similarity) {
                            best_neighbor = neighbor;
                            best_neighbor_similarity = neighbor_similarity;

                            // early break...
                            break;
                        }
                    }

                    // record the similarity of this node if
                    // it is the best seen so far
                    // (will be used with expansion parameter at next iteraion)
                    if (best_neighbor_similarity > highest_similarity) {
                        highest_similarity = best_neighbor_similarity;
                    }

                    // No node provides higher similarity
                    // => we reached the end of this track...
                    // => restart and
                    if (best_neighbor == null) {
                        break restart;
                    }

                    current_node = best_neighbor;
                }
            } // restart

            current_node = getRandomNode();
            result.incRestarts();
        }


        for (Map.Entry<T, Double> entry : visited_nodes.entrySet()) {
            result.getNeighbors().add(
                    new Neighbor(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    /**
     * Writes the graph as a GEXF file (to be used in Gephi, for example).
     *
     * @param filename
     * @throws FileNotFoundException if filename is invalid
     * @throws IOException if cannot write to file
     */
    public final void writeGEXF(final String filename)
            throws FileNotFoundException, IOException {
        Writer out = new OutputStreamWriter(
                new BufferedOutputStream(new FileOutputStream(filename)));
        out.write(GEXF_HEADER);

        // Write nodes
        out.write("<nodes>\n");
        int node_id = 0;
        Map<T, Integer> node_registry = new IdentityHashMap<T, Integer>();

        for (T node : map.keySet()) {
            node_registry.put(node, node_id);
            out.write("<node id=\"" + node_id
                    + "\" label=\"" + node.toString() + "\" />\n");
            node_id++;
        }
        out.write("</nodes>\n");

        // Write edges
        out.write("<edges>\n");
        int i = 0;
        for (T source : map.keySet()) {
            int source_id = node_registry.get(source);
            for (Neighbor<T> target : this.getNeighbors(source)) {
                int target_id = node_registry.get(target.getNode());
                out.write("<edge id=\"" + i + "\" source=\"" + source_id + "\" "
                        + "target=\"" + target_id + "\" "
                        + "weight=\"" + target.getSimilarity() + "\" />\n");
                i++;
            }
        }

        out.write("</edges>\n");

        // End the file
        out.write("</graph>\n"
                + "</gexf>");
        out.close();
    }

    private static final String GEXF_HEADER
            = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<gexf xmlns=\"http://www.gexf.net/1.2draft\" version=\"1.2\">\n"
            + "<meta>\n"
            + "<creator>info.debatty.java.graphs.Graph</creator>\n"
            + "<description></description>\n"
            + "</meta>\n"
            + "<graph mode=\"static\" defaultedgetype=\"directed\">\n";



    /**
     * Add a node to the online graph using exhaustive search approach.
     * Adding a node requires to compute the similarity between the new node
     * and every other node in the graph...
     * @param new_node
     * @return
     */
    public final int add(final T new_node) {
        if (containsKey(new_node)) {
            throw new IllegalArgumentException(
                    "This graph already contains this node");
        }

        NeighborList nl = new NeighborList(k);

        for (T other_node : getNodes()) {
            double sim = similarity.similarity(
                    new_node, other_node);
            nl.add(new Neighbor(other_node, sim));
            getNeighbors(other_node).add(new Neighbor(new_node, sim));
        }

        this.put(new_node, nl);

        nodes = null;
        return (size() - 1);

    }

    /**
     * Add a node to the online graph, using approximate online graph building
     * algorithm presented in "Fast Online k-nn Graph Building" by Debatty
     * et al. Default speedup is 4 compared to exhaustive search.
     *
     * @param node
     */
    public final void fastAdd(final T node) {
        fastAdd(node, OnlineConfig.getDefault());
    }

    /**
     * Add a node to the online graph, using approximate online graph building
     * algorithm presented in "Fast Online k-nn Graph Building" by Debatty
     * et al.
     *
     * @param new_node
     * @param conf
     */
    public final void fastAdd(
            final T new_node,
            final OnlineConfig conf) {

        if (containsKey(new_node)) {
            throw new IllegalArgumentException(
                    "This graph already contains this node");
        }

        // 3. Search the neighbors of the new node
        conf.setK(getK());
        NeighborList neighborlist = fastSearch(new_node, conf).getNeighbors();
        put(new_node, neighborlist);

        // 4. Update existing edges
        // Nodes to analyze at this iteration
        LinkedList<T> analyze = new LinkedList<T>();

        // Nodes to analyze at next iteration
        LinkedList<T> next_analyze = new LinkedList<T>();

        // List of already analyzed nodes
        HashMap<T, Boolean> visited = new HashMap<T, Boolean>();

        // Fill the list of nodes to analyze
        for (Neighbor<T> neighbor : getNeighbors(new_node)) {
            analyze.add(neighbor.getNode());
        }

        StatisticsContainer stats = new StatisticsContainer();

        for (int d = 0; d < conf.getUpdateDepth(); d++) {
            while (!analyze.isEmpty()) {
                T other = analyze.pop();
                NeighborList other_neighborlist = getNeighbors(other);

                // Add neighbors to the list of nodes to analyze at
                // next iteration
                for (Neighbor<T> other_neighbor : other_neighborlist) {
                    if (!visited.containsKey(other_neighbor.getNode())) {
                        next_analyze.add(other_neighbor.getNode());
                    }
                }

                // Try to add the new node (if sufficiently similar)
                stats.incAddSimilarities();
                other_neighborlist.add(new Neighbor(
                        new_node,
                        similarity.similarity(
                                new_node,
                                other)));

                visited.put(other, Boolean.TRUE);
            }

            analyze = next_analyze;
            next_analyze = new LinkedList<T>();
        }

        nodes = null;
    }

    /**
     * Remove a node from the graph (and update the graph) using fast
     * approximate algorithm.
     * @param node_to_remove
     */
    public final void fastRemove(final T node_to_remove) {
        fastRemove(node_to_remove, OnlineConfig.getDefault());

    }

    /**
     * Remove a node from the graph (and update the graph) using fast
     * approximate algorithm.
     * @param node_to_remove
     * @param conf
     */
    public final void fastRemove(
            final T node_to_remove,
            final OnlineConfig conf) {

        // Build the list of nodes to update
        LinkedList<T> nodes_to_update = new LinkedList<T>();

        for (T node : getNodes()) {
            NeighborList nl = getNeighbors(node);
            if (nl.containsNode(node_to_remove)) {
                nodes_to_update.add(node);
                nl.removeNode(node_to_remove);
            }
        }

        // Build the list of candidates
        LinkedList<T> initial_candidates = new LinkedList<T>();
        initial_candidates.add(node_to_remove);
        initial_candidates.addAll(nodes_to_update);

        LinkedList<T> candidates = findNeighbors(
                initial_candidates, conf.getUpdateDepth());
        while (candidates.contains(node_to_remove)) {
            candidates.remove(node_to_remove);
        }

        StatisticsContainer stats = new StatisticsContainer();

        // Update the nodes_to_update
        for (T node_to_update : nodes_to_update) {
            NeighborList nl_to_update = getNeighbors(node_to_update);
            for (T candidate : candidates) {
                if (candidate.equals(node_to_update)) {
                    continue;
                }

                stats.incRemoveSimilarities();
                double sim = similarity.similarity(
                        node_to_update,
                        candidate);

                nl_to_update.add(new Neighbor(candidate, sim));
            }
        }

        // Remove node_to_remove
        map.remove(node_to_remove);
        nodes = null;
    }

    /**
     * Count the number of edges/neighbors that are the same (based on
     * similarity) in both graphs.
     *
     * @param other
     * @return
     */
    public final int compare(final Graph<T> other) {
        int correct_edges = 0;
        for (T node : map.keySet()) {
            correct_edges += getNeighbors(node).countCommons(
                    other.getNeighbors(node));
        }
        return correct_edges;
    }

    @Override
    public final String toString() {
        return map.toString();
    }

    private static final int HASH_BASE = 3;
    private static final int HASH_MULT = 23;

    @Override
    public final int hashCode() {
        int hash = HASH_BASE;
        hash = HASH_MULT * hash + this.map.hashCode();
        return hash;
    }

    @Override
    public final boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Graph<?> other = (Graph<?>) obj;

        return this.map.equals(other.map);
    }
}
