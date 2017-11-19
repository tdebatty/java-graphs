/*
 * The MIT License
 *
 * Copyright 2017 tibo.
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

/**
 *
 * @author tibo
 * @param <T> type of nodes in the graph
 */
public class FastSearchResult<T> {

    private int similarities;
    private int restarts;
    private int boundary_restarts;
    private final NeighborList neighbors;
    private T boundary_node;

    FastSearchResult(final int k) {
        this.neighbors = new NeighborList(k);
    }

    /**
     * Number of computed similarities.
     * @return
     */
    public final int getSimilarities() {
        return similarities;
    }

    /**
     * Number of restarts because we reached a local maximum.
     * @return
     */
    public final int getRestarts() {
        return restarts;
    }

    /**
     * Number of restarts because we reached the boundary of the partition.
     * @return
     */
    public final int getBoundaryRestarts() {
        return boundary_restarts;
    }

    /**
     * Get the k most similar neighbors that we found.
     * @return
     */
    public final NeighborList getNeighbors() {
        return neighbors;
    }

    final void incSimilarities() {
        similarities++;
    }

    final void incRestarts() {
        restarts++;
    }

    final void incBoundaryRestarts() {
        boundary_restarts++;
    }

    /**
     * If we stopped searching because of a boundary, this will contain
     * the boundary node.
     * @return
     */
    public final T getBoundaryNode() {
        return boundary_node;
    }

    /**
     *
     * @param boundary_node
     */
    final void setBoundaryNode(final T boundary_node) {
        this.boundary_node = boundary_node;
    }
}
