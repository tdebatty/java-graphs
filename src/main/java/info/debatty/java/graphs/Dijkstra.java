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
 *
 * @author Thibault Debatty
 */
public class Dijkstra {

    private final Graph graph;
    private final Set<Node> settledNodes;
    private final Set<Node> unSettledNodes;
    private final Map<Node, Node> predecessors;
    private final Map<Node, Integer> distances;

    /**
     * Compute the shortest path from source node to every other node in the 
     * graph.
     * @param graph
     * @param source 
     */
    public Dijkstra(Graph graph, Node source) {

        this.graph = graph;

        settledNodes = new HashSet<Node>();
        unSettledNodes = new HashSet<Node>();
        distances = new HashMap<Node, Integer>();
        predecessors = new HashMap<Node, Node>();

        distances.put(source, 0);
        unSettledNodes.add(source);

        while (unSettledNodes.size() > 0) {
            Node node = getMinimum(unSettledNodes);
            settledNodes.add(node);
            unSettledNodes.remove(node);
            findMinimalDistances(node);
        }
    }

    /*
     * This method returns the path from the source to the selected target and
     * NULL if no path exists
     */
    public LinkedList<Node> getPath(Node target) {
        LinkedList<Node> path = new LinkedList<Node>();
        Node step = target;
        // check if a path exists
        if (predecessors.get(step) == null) {
            return null;
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

    public int getBiggestDistance() {
        int biggest = 0;
        for (Integer distance : distances.values()) {
            if (distance > biggest) {
                biggest = distance;
            }
        }
        return biggest;
    }

    private void findMinimalDistances(Node node) {

        for (Neighbor neighbor : graph.get(node)) {
            Node target = neighbor.node;

            if (getShortestDistance(target) > (getShortestDistance(node) + 1)) {
                distances.put(target, getShortestDistance(node) + 1);
                predecessors.put(target, node);
                unSettledNodes.add(target);
            }
        }
    }

    private Node getMinimum(Set<Node> nodes) {
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

    private int getShortestDistance(Node destination) {
        Integer d = distances.get(destination);
        if (d == null) {
            return Integer.MAX_VALUE;
        } else {
            return d;
        }
    }
}
