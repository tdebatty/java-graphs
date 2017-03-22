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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * Compute the shortest path (measured as the number of 'hops' from this source
 * node to every other node in the graph using Dijkstra algorithm.
 *
 * @author Thibault Debatty
 * @param <T> type of nodes in the graph
 */
public class Dijkstra<T> {

    private final Graph graph;
    private final Set<T> settled_nodes;
    private final Set<T> unsettled_nodes;
    private final Map<T, T> predecessors;
    private final Map<T, Integer> distances;

    /**
     * Compute the shortest path from source node to every other node in the
     * graph.
     *
     * @param graph to use for computing path
     * @param source node from which to compute distance to every other node
     */
    public Dijkstra(final Graph graph, final T source) {

        this.graph = graph;

        settled_nodes = new HashSet<T>();
        unsettled_nodes = new HashSet<T>();
        distances = new HashMap<T, Integer>();
        predecessors = new HashMap<T, T>();

        distances.put(source, 0);
        unsettled_nodes.add(source);

        while (unsettled_nodes.size() > 0) {
            T node = getMinimum(unsettled_nodes);
            settled_nodes.add(node);
            unsettled_nodes.remove(node);
            findMinimalDistances(node);
        }
    }

    /**
     * Return the path from the source to the selected target.
     *
     * @param target node to which we search the path
     * @return the path from the source to the selected target
     * @throws java.lang.Exception if no path exists to this target
     */
    public final LinkedList<T> getPath(final T target) throws Exception {
        LinkedList<T> path = new LinkedList<T>();
        T step = target;
        // check if a path exists
        if (predecessors.get(step) == null) {
            throw new Exception("No path found to this target");
        }
        path.add(step);
        while (predecessors.get(step) != null) {
            step = predecessors.get(step);
            path.add(step);
        }
        // Put it into the correct order
        Collections.reverse(path);
        return path;
    }

    /**
     * Return the distance (measured as the number of hops) to the most distant
     * node.
     *
     * @return the distance (measured as the number of hops) to the most distant
     * node
     */
    public final int getLargestDistance() {
        int largest = 0;
        for (Integer distance : distances.values()) {
            if (distance > largest) {
                largest = distance;
            }
        }
        return largest;
    }

    private void findMinimalDistances(final T node) {
        if (!graph.containsKey(node) || graph.getNeighbors(node) == null) {
            return;
        }

        for (Neighbor<T> neighbor : graph.getNeighbors(node)) {
            T target = neighbor.getNode();

            if (getShortestDistance(target) > (getShortestDistance(node) + 1)) {
                distances.put(target, getShortestDistance(node) + 1);
                predecessors.put(target, node);
                unsettled_nodes.add(target);
            }
        }
    }

    private T getMinimum(final Set<T> nodes) {
        T minimum = null;
        for (T node : nodes) {
            if (minimum == null) {
                minimum = node;
            } else {
                if (getShortestDistance(node) < getShortestDistance(minimum)) {
                    minimum = node;
                }
            }
        }
        return minimum;
    }

    private int getShortestDistance(final T destination) {
        Integer d = distances.get(destination);
        if (d == null) {
            return Integer.MAX_VALUE;
        } else {
            return d;
        }
    }
}
