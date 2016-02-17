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
 */
public class Dijkstra {

    private final Graph graph;
    private final Set<Node> settled_nodes;
    private final Set<Node> unsettled_nodes;
    private final Map<Node, Node> predecessors;
    private final Map<Node, Integer> distances;

    /**
     * Compute the shortest path from source node to every other node in the
     * graph.
     *
     * @param graph to use for computing path
     * @param source node from which to compute distance to every other node
     */
    public Dijkstra(final Graph graph, final Node source) {

        this.graph = graph;

        settled_nodes = new HashSet<Node>();
        unsettled_nodes = new HashSet<Node>();
        distances = new HashMap<Node, Integer>();
        predecessors = new HashMap<Node, Node>();

        distances.put(source, 0);
        unsettled_nodes.add(source);

        while (unsettled_nodes.size() > 0) {
            Node node = getMinimum(unsettled_nodes);
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
    public final LinkedList<Node> getPath(final Node target) throws Exception {
        LinkedList<Node> path = new LinkedList<Node>();
        Node step = target;
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

    private void findMinimalDistances(final Node node) {
        if (!graph.containsKey(node) || graph.get(node) == null) {
            return;
        }

        for (Neighbor neighbor : graph.get(node)) {
            Node target = neighbor.node;

            if (getShortestDistance(target) > (getShortestDistance(node) + 1)) {
                distances.put(target, getShortestDistance(node) + 1);
                predecessors.put(target, node);
                unsettled_nodes.add(target);
            }
        }
    }

    private Node getMinimum(final Set<Node> nodes) {
        Node minimum = null;
        for (Node node : nodes) {
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

    private int getShortestDistance(final Node destination) {
        Integer d = distances.get(destination);
        if (d == null) {
            return Integer.MAX_VALUE;
        } else {
            return d;
        }
    }
}
